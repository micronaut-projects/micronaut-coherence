package io.micronaut.coherence.docs.injection.subscriber

import com.tangosol.net.topic.Subscriber
import io.micronaut.coherence.docs.injection.publisher.OrderPublishers
import io.micronaut.coherence.docs.injection.topics.OrderProcessor
import io.micronaut.coherence.docs.injection.topics.SomeBean
import io.micronaut.coherence.docs.injection.topics.TopicsBean
import io.micronaut.coherence.docs.model.Order
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Property(name = "spec.name", value = "TopicInjectionTest")
@MicronautTest
class TopicInjectionTest {

    @Inject
    lateinit var topicsBean: TopicsBean

    @Inject
    lateinit var someBean: SomeBean

    @Inject
    lateinit var orderProcessor: OrderProcessor

    @Inject
    lateinit var orderPublishers: OrderPublishers

    @Inject
    lateinit var orderController: io.micronaut.coherence.docs.injection.publisher.OrderController

    @Inject
    lateinit var orderSubscribers: OrderSubscribers

    @Inject
    lateinit var orderSubscriberController: OrderController

    @Inject
    lateinit var filteredOrderController: FilteredOrderController

    @Inject
    lateinit var groupOrderController: GroupOrderController

    @Inject
    lateinit var transformedOrderController: TransformedOrderController

    @Test
    fun testInjectNamedTopic() {
        assertEquals("people", topicsBean.people.name)
        assertEquals("orders", topicsBean.orders.name)
        assertEquals("orders", someBean.topic.name)
        assertEquals("orders", topicsBean.topic.name)
        assertEquals("orders", orderProcessor.orders.name)
        // the "Customers" session owns a different topic named "orders"
        assertNotEquals(topicsBean.orders.service, topicsBean.topic.service)
        assertEquals(topicsBean.topic.service, orderProcessor.orders.service)
    }

    @Test
    fun testPublishAndSubscribe() {
        val order = Order(1, "homer", "AB1234")
        orderPublishers.orders.publish(order).join()
        assertEquals("AB1234", receive(orderSubscribers.orders).productId)
        assertEquals("AB1234", receive(orderSubscribers.subscriber).productId)
        assertEquals("AB1234", receive(orderSubscriberController.topic).productId)
        assertEquals("AB1234", receive(orderSubscribers.filteredOrders).productId)
        assertEquals("AB1234", receive(filteredOrderController.topic).productId)
        assertEquals("AB1234", receive(orderSubscribers.productIds))
        assertEquals("AB1234", receive(transformedOrderController.topic))
        // the two subscribers of the "accounts" group share the messages of the topic: one of them receives the order
        val received = CompletableFuture.anyOf(orderSubscribers.accountOrders.receive(), groupOrderController.topic.receive())
            .thenApply { (it as Subscriber.Element<*>).value as Order }
            .get(1, TimeUnit.MINUTES)
        assertEquals("AB1234", received.productId)

        orderPublishers.publisher.publish(Order(2, "marge", "XY9999")).join()
        orderController.topic.publish(Order(3, "bart", "AB1234")).join()
        // the filtered subscribers only receive the orders of product AB1234
        assertEquals(3, receive(orderSubscribers.filteredOrders).orderId)
        assertEquals(3, receive(filteredOrderController.topic).orderId)
        assertEquals(2, receive(orderSubscribers.orders).orderId)
        assertEquals(3, receive(orderSubscribers.orders).orderId)
    }

    @Test
    fun testPublishAndSubscribeInSession() {
        orderPublishers.customerOrders.publish(Order(4, "lisa", "AB1234")).join()
        assertEquals(4, receive(orderSubscribers.customerOrders).orderId)
    }

    private fun <T> receive(subscriber: Subscriber<T>): T = subscriber.receive().get(1, TimeUnit.MINUTES).value
}
