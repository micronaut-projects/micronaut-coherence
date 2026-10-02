package io.micronaut.coherence.docs.injection.subscriber

import com.tangosol.net.topic.Subscriber
import io.micronaut.coherence.docs.injection.publisher.OrderPublishers
import io.micronaut.coherence.docs.injection.topics.OrderProcessor
import io.micronaut.coherence.docs.injection.topics.SomeBean
import io.micronaut.coherence.docs.injection.topics.TopicsBean
import io.micronaut.coherence.docs.model.Order
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Property(name = "spec.name", value = "TopicInjectionTest")
@MicronautTest
class TopicInjectionSpec extends Specification {

    @Inject
    TopicsBean topicsBean

    @Inject
    SomeBean someBean

    @Inject
    OrderProcessor orderProcessor

    @Inject
    OrderPublishers orderPublishers

    @Inject
    io.micronaut.coherence.docs.injection.publisher.OrderController orderController

    @Inject
    OrderSubscribers orderSubscribers

    @Inject
    OrderController orderSubscriberController

    @Inject
    FilteredOrderController filteredOrderController

    @Inject
    GroupOrderController groupOrderController

    @Inject
    TransformedOrderController transformedOrderController

    void "test inject named topic"() {
        expect:
        topicsBean.people.name == "people"
        topicsBean.orders.name == "orders"
        someBean.topic.name == "orders"
        topicsBean.topic.name == "orders"
        orderProcessor.orders.name == "orders"
        // the "Customers" session owns a different topic named "orders"
        !topicsBean.orders.service.is(topicsBean.topic.service)
        topicsBean.topic.service.is(orderProcessor.orders.service)
    }

    void "test publish and subscribe"() {
        when:
        orderPublishers.orders.publish(new Order(1, "homer", "AB1234")).join()

        then:
        receive(orderSubscribers.orders).productId == "AB1234"
        receive(orderSubscribers.subscriber).productId == "AB1234"
        receive(orderSubscriberController.topic).productId == "AB1234"
        receive(orderSubscribers.filteredOrders).productId == "AB1234"
        receive(filteredOrderController.topic).productId == "AB1234"
        receive(orderSubscribers.productIds) == "AB1234"
        receive(transformedOrderController.topic) == "AB1234"
        // the two subscribers of the "accounts" group share the messages of the topic: one of them receives the order
        CompletableFuture.anyOf(orderSubscribers.accountOrders.receive(), groupOrderController.topic.receive())
                .thenApply { ((Subscriber.Element) it).value }
                .get(1, TimeUnit.MINUTES).productId == "AB1234"

        when:
        orderPublishers.publisher.publish(new Order(2, "marge", "XY9999")).join()
        orderController.topic.publish(new Order(3, "bart", "AB1234")).join()

        then: "the filtered subscribers only receive the orders of product AB1234"
        receive(orderSubscribers.filteredOrders).orderId == 3
        receive(filteredOrderController.topic).orderId == 3
        receive(orderSubscribers.orders).orderId == 2
        receive(orderSubscribers.orders).orderId == 3
    }

    void "test publish and subscribe in session"() {
        when:
        orderPublishers.customerOrders.publish(new Order(4, "lisa", "AB1234")).join()

        then:
        receive(orderSubscribers.customerOrders).orderId == 4
    }

    private static <T> T receive(Subscriber<T> subscriber) {
        subscriber.receive().get(1, TimeUnit.MINUTES).value
    }
}
