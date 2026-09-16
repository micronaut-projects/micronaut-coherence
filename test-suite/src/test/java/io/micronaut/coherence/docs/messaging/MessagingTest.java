package io.micronaut.coherence.docs.messaging;

import com.tangosol.net.Session;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import io.micronaut.coherence.docs.model.Book;
import io.micronaut.coherence.docs.model.Product;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "MessagingTest")
@MicronautTest
class MessagingTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    Session session;

    @Inject
    BookClient bookClient;

    @Inject
    ProductListener productListener;

    @Inject
    ProductElementListener productElementListener;

    @Inject
    SyncCommitListener syncCommitListener;

    @Inject
    AsyncCommitListener asyncCommitListener;

    @Inject
    ManualCommitListener manualCommitListener;

    @BeforeEach
    void awaitSubscribers() {
        MessagingHelper.awaitSubscribed(applicationContext);
    }

    @Test
    void testPublisherAndListener() {
        // tag::usage[]
        ProductClient client = applicationContext.getBean(ProductClient.class);
        client.sendProduct("Blue Trainers");
        // end::usage[]
        client.sendProduct("my-products", "Red Trainers");

        await(() -> productListener.products, List.of("Blue Trainers", "Red Trainers"));
    }

    @Test
    void testReactivePublisher() throws Exception {
        Subscriber<Book> subscriber = session.<Book>getTopic("books").createSubscriber();

        Book sent = bookClient.sendBook(Mono.just(new Book("Micronaut in Action", 400))).block();
        assertEquals("Micronaut in Action", sent.getTitle());
        assertEquals("Micronaut in Action", subscriber.receive().get(1, TimeUnit.MINUTES).getValue().getTitle());

        List<Book> books = bookClient.sendBooks(Flux.just(new Book("One", 1), new Book("Two", 2))).collectList().block();
        assertEquals(List.of("One", "Two"), books.stream().map(Book::getTitle).toList());
        assertEquals("One", subscriber.receive().get(1, TimeUnit.MINUTES).getValue().getTitle());
        assertEquals("Two", subscriber.receive().get(1, TimeUnit.MINUTES).getValue().getTitle());
    }

    @Test
    void testCommitStrategies() throws Exception {
        try (Publisher<Product> publisher = session.<Product>getTopic("products").createPublisher()) {
            publisher.publish(new Product("Trainers", "Acme", 3)).get(1, TimeUnit.MINUTES);
        }
        await(() -> productElementListener.elements.size(), 1);
        assertEquals("Trainers", productElementListener.elements.get(0).getValue().getName());
        await(() -> syncCommitListener.products.size(), 1);
        await(() -> asyncCommitListener.products.size(), 1);
        await(() -> manualCommitListener.products.size(), 1);
    }

    @Test
    void testForwarding() throws Exception {
        Subscriber<Integer> quantities = session.<Integer>getTopic("product-quantities").createSubscriber();
        try (Publisher<Product> publisher = session.<Product>getTopic("awesome-products").createPublisher()) {
            publisher.publish(new Product("Trainers", "Acme", 7)).get(1, TimeUnit.MINUTES);
        }
        // both listeners forward the quantity of the product
        List<Integer> forwarded = List.of(
                quantities.receive().get(1, TimeUnit.MINUTES).getValue(),
                quantities.receive().get(1, TimeUnit.MINUTES).getValue());
        assertEquals(List.of(7, 7), forwarded);
    }

    private static <T> void await(Supplier<T> actual, T expected) {
        long end = System.currentTimeMillis() + 30_000;
        while (!expected.equals(actual.get()) && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(expected, actual.get());
    }
}
