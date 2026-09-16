package io.micronaut.coherence.docs.messaging

import com.tangosol.net.Session
import com.tangosol.net.topic.Publisher
import com.tangosol.net.topic.Subscriber
import io.micronaut.coherence.docs.model.Book
import io.micronaut.coherence.docs.model.Product
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.TimeUnit

@Property(name = "spec.name", value = "MessagingTest")
@MicronautTest
class MessagingSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    @Inject
    Session session

    @Inject
    BookClient bookClient

    @Inject
    ProductListener productListener

    @Inject
    ProductElementListener productElementListener

    @Inject
    SyncCommitListener syncCommitListener

    @Inject
    AsyncCommitListener asyncCommitListener

    @Inject
    ManualCommitListener manualCommitListener

    void setup() {
        MessagingHelper.awaitSubscribed(applicationContext)
    }

    void "test publisher and listener"() {
        when:
        // tag::usage[]
        ProductClient client = applicationContext.getBean(ProductClient)
        client.sendProduct("Blue Trainers")
        // end::usage[]
        client.sendProduct("my-products", "Red Trainers")

        then:
        new PollingConditions(timeout: 30).eventually {
            assert productListener.products == ["Blue Trainers", "Red Trainers"]
        }
    }

    void "test reactive publisher"() {
        given:
        Subscriber<Book> subscriber = session.<Book>getTopic("books").createSubscriber()

        when:
        Book sent = bookClient.sendBook(Mono.just(new Book("Micronaut in Action", 400))).block()

        then:
        sent.title == "Micronaut in Action"
        subscriber.receive().get(1, TimeUnit.MINUTES).value.title == "Micronaut in Action"

        when:
        List<Book> books = bookClient.sendBooks(Flux.just(new Book("One", 1), new Book("Two", 2))).collectList().block()

        then:
        books*.title == ["One", "Two"]
        subscriber.receive().get(1, TimeUnit.MINUTES).value.title == "One"
        subscriber.receive().get(1, TimeUnit.MINUTES).value.title == "Two"
    }

    void "test commit strategies"() {
        when:
        session.<Product>getTopic("products").createPublisher().withCloseable { Publisher<Product> publisher ->
            publisher.publish(new Product("Trainers", "Acme", 3)).get(1, TimeUnit.MINUTES)
        }

        then:
        new PollingConditions(timeout: 30).eventually {
            assert productElementListener.elements.size() == 1
            assert productElementListener.elements[0].value.name == "Trainers"
            assert syncCommitListener.products.size() == 1
            assert asyncCommitListener.products.size() == 1
            assert manualCommitListener.products.size() == 1
        }
    }

    void "test forwarding"() {
        given:
        Subscriber<Integer> quantities = session.<Integer>getTopic("product-quantities").createSubscriber()

        when:
        session.<Product>getTopic("awesome-products").createPublisher().withCloseable { Publisher<Product> publisher ->
            publisher.publish(new Product("Trainers", "Acme", 7)).get(1, TimeUnit.MINUTES)
        }

        then: "both listeners forward the quantity of the product"
        [quantities.receive().get(1, TimeUnit.MINUTES).value, quantities.receive().get(1, TimeUnit.MINUTES).value] == [7, 7]
    }
}
