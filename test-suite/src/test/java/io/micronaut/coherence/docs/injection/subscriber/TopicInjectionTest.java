package io.micronaut.coherence.docs.injection.subscriber;

import com.tangosol.net.topic.Subscriber;
import io.micronaut.coherence.docs.injection.publisher.OrderPublishers;
import io.micronaut.coherence.docs.injection.topics.OrderProcessor;
import io.micronaut.coherence.docs.injection.topics.SomeBean;
import io.micronaut.coherence.docs.injection.topics.TopicsBean;
import io.micronaut.coherence.docs.model.Order;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Property(name = "spec.name", value = "TopicInjectionTest")
@MicronautTest
class TopicInjectionTest {

    @Inject
    TopicsBean topicsBean;

    @Inject
    SomeBean someBean;

    @Inject
    OrderProcessor orderProcessor;

    @Inject
    OrderPublishers orderPublishers;

    @Inject
    io.micronaut.coherence.docs.injection.publisher.OrderController orderController;

    @Inject
    OrderSubscribers orderSubscribers;

    @Inject
    OrderController orderSubscriberController;

    @Inject
    FilteredOrderController filteredOrderController;

    @Inject
    GroupOrderController groupOrderController;

    @Inject
    TransformedOrderController transformedOrderController;

    @Test
    void testInjectNamedTopic() {
        assertEquals("people", topicsBean.getPeople().getName());
        assertEquals("orders", topicsBean.getOrders().getName());
        assertEquals("orders", someBean.getTopic().getName());
        assertEquals("orders", topicsBean.getTopic().getName());
        assertEquals("orders", orderProcessor.getOrders().getName());
        // the "Customers" session owns a different topic named "orders"
        assertNotEquals(topicsBean.getOrders().getService(), topicsBean.getTopic().getService());
        assertEquals(topicsBean.getTopic().getService(), orderProcessor.getOrders().getService());
    }

    @Test
    void testPublishAndSubscribe() throws Exception {
        Order order = new Order(1, "homer", "AB1234");
        orderPublishers.getOrders().publish(order).join();
        assertEquals("AB1234", receive(orderSubscribers.orders).getProductId());
        assertEquals("AB1234", receive(orderSubscribers.subscriber).getProductId());
        assertEquals("AB1234", receive(orderSubscriberController.getTopic()).getProductId());
        assertEquals("AB1234", receive(orderSubscribers.filteredOrders).getProductId());
        assertEquals("AB1234", receive(filteredOrderController.getTopic()).getProductId());
        assertEquals("AB1234", receive(orderSubscribers.productIds));
        assertEquals("AB1234", receive(transformedOrderController.getTopic()));
        // the two subscribers of the "accounts" group share the messages of the topic: one of them receives the order
        Order received = (Order) CompletableFuture.anyOf(orderSubscribers.accountOrders.receive(), groupOrderController.getTopic().receive())
                .thenApply(element -> ((Subscriber.Element<?>) element).getValue())
                .get(1, TimeUnit.MINUTES);
        assertEquals("AB1234", received.getProductId());

        orderPublishers.getPublisher().publish(new Order(2, "marge", "XY9999")).join();
        orderController.getTopic().publish(new Order(3, "bart", "AB1234")).join();
        // the filtered subscribers only receive the orders of product AB1234
        assertEquals(3, receive(orderSubscribers.filteredOrders).getOrderId());
        assertEquals(3, receive(filteredOrderController.getTopic()).getOrderId());
        assertEquals(2, receive(orderSubscribers.orders).getOrderId());
        assertEquals(3, receive(orderSubscribers.orders).getOrderId());
    }

    @Test
    void testPublishAndSubscribeInSession() throws Exception {
        orderPublishers.getCustomerOrders().publish(new Order(4, "lisa", "AB1234")).join();
        assertEquals(4, receive(orderSubscribers.customerOrders).getOrderId());
    }

    private static <T> T receive(Subscriber<T> subscriber) throws Exception {
        return subscriber.receive().get(1, TimeUnit.MINUTES).getValue();
    }
}
