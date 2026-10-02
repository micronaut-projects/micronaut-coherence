package io.micronaut.coherence.docs.messaging

import com.tangosol.net.Session
import io.micronaut.coherence.docs.model.Book
import io.micronaut.coherence.docs.model.Product
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

@Property(name = "spec.name", value = "MessagingTest")
@MicronautTest
class MessagingTest {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var bookClient: BookClient

    @Inject
    lateinit var productListener: ProductListener

    @Inject
    lateinit var productElementListener: ProductElementListener

    @Inject
    lateinit var syncCommitListener: SyncCommitListener

    @Inject
    lateinit var asyncCommitListener: AsyncCommitListener

    @Inject
    lateinit var manualCommitListener: ManualCommitListener

    @BeforeEach
    fun awaitSubscribers() {
        MessagingHelper.awaitSubscribed(applicationContext)
    }

    @Test
    fun testPublisherAndListener() {
        // tag::usage[]
        val client = applicationContext.getBean(ProductClient::class.java)
        client.sendProduct("Blue Trainers")
        // end::usage[]
        client.sendProduct("my-products", "Red Trainers")

        await({ productListener.products.toList() }, listOf("Blue Trainers", "Red Trainers"))
    }

    @Test
    fun testReactivePublisher() {
        val subscriber = session.getTopic<Book>("books").createSubscriber()

        val sent = bookClient.sendBook(Mono.just(Book("Micronaut in Action", 400))).block()!!
        assertEquals("Micronaut in Action", sent.title)
        assertEquals("Micronaut in Action", subscriber.receive().get(1, TimeUnit.MINUTES).value.title)

        val books = bookClient.sendBooks(Flux.just(Book("One", 1), Book("Two", 2))).collectList().block()!!
        assertEquals(listOf("One", "Two"), books.map { it.title })
        assertEquals("One", subscriber.receive().get(1, TimeUnit.MINUTES).value.title)
        assertEquals("Two", subscriber.receive().get(1, TimeUnit.MINUTES).value.title)
    }

    @Test
    fun testCommitStrategies() {
        session.getTopic<Product>("products").createPublisher().use { publisher ->
            publisher.publish(Product("Trainers", "Acme", 3)).get(1, TimeUnit.MINUTES)
        }
        await({ productElementListener.elements.size }, 1)
        assertEquals("Trainers", productElementListener.elements[0].value.name)
        await({ syncCommitListener.products.size }, 1)
        await({ asyncCommitListener.products.size }, 1)
        await({ manualCommitListener.products.size }, 1)
    }

    @Test
    fun testForwarding() {
        val quantities = session.getTopic<Int>("product-quantities").createSubscriber()
        session.getTopic<Product>("awesome-products").createPublisher().use { publisher ->
            publisher.publish(Product("Trainers", "Acme", 7)).get(1, TimeUnit.MINUTES)
        }
        // both listeners forward the quantity of the product
        val forwarded = listOf(
            quantities.receive().get(1, TimeUnit.MINUTES).value,
            quantities.receive().get(1, TimeUnit.MINUTES).value)
        assertEquals(listOf(7, 7), forwarded)
    }

    private fun <T> await(actual: () -> T, expected: T) {
        val end = System.currentTimeMillis() + 30_000
        while (actual() != expected && System.currentTimeMillis() < end) {
            Thread.sleep(50)
        }
        assertEquals(expected, actual())
    }
}
