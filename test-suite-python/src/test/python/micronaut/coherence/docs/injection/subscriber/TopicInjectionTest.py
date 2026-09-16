from typing import Annotated

from jakarta.inject import Inject
from java.util.concurrent import CompletableFuture, TimeUnit
from micronaut.coherence.examples.model import Order
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from ..publisher.OrderController import OrderController as OrderPublisherController
from ..publisher.OrderPublishers import OrderPublishers
from ..topics.OrderProcessor import OrderProcessor
from ..topics.SomeBean import SomeBean
from ..topics.TopicsBean import TopicsBean
from .FilteredOrderController import FilteredOrderController
from .GroupOrderController import GroupOrderController
from .OrderController import OrderController
from .OrderSubscribers import OrderSubscribers
from .TransformedOrderController import TransformedOrderController


def receive(subscriber):
    return subscriber.receive().get(1, TimeUnit.MINUTES).getValue()


@Property(name="spec.name", value="TopicInjectionTest")
@MicronautTest
class TopicInjectionTest:
    topics_bean: Annotated[TopicsBean, Inject]
    some_bean: Annotated[SomeBean, Inject]
    order_processor: Annotated[OrderProcessor, Inject]
    order_publishers: Annotated[OrderPublishers, Inject]
    order_controller: Annotated[OrderPublisherController, Inject]
    order_subscribers: Annotated[OrderSubscribers, Inject]
    order_subscriber_controller: Annotated[OrderController, Inject]
    filtered_order_controller: Annotated[FilteredOrderController, Inject]
    group_order_controller: Annotated[GroupOrderController, Inject]
    transformed_order_controller: Annotated[TransformedOrderController, Inject]

    @Test
    def test_inject_named_topic(self):
        assert self.topics_bean.people.getName() == "people"
        assert self.topics_bean.orders.getName() == "orders"
        assert self.some_bean.topic.getName() == "orders"
        assert self.topics_bean.topic.getName() == "orders"
        assert self.order_processor.orders.getName() == "orders"
        # the "Customers" session owns a different topic named "orders"
        assert not self.topics_bean.orders.getService().equals(self.topics_bean.topic.getService())
        assert self.topics_bean.topic.getService().equals(self.order_processor.orders.getService())

    @Test
    def test_publish_and_subscribe(self):
        self.order_publishers.orders.publish(Order(1, "homer", "AB1234")).join()
        assert receive(self.order_subscribers.orders).getProductId() == "AB1234"
        assert receive(self.order_subscribers.subscriber).getProductId() == "AB1234"
        assert receive(self.order_subscriber_controller.topic).getProductId() == "AB1234"
        assert receive(self.order_subscribers.filtered_orders).getProductId() == "AB1234"
        assert receive(self.filtered_order_controller.topic).getProductId() == "AB1234"
        assert receive(self.order_subscribers.product_ids) == "AB1234"
        assert receive(self.transformed_order_controller.topic) == "AB1234"
        # the two subscribers of the "accounts" group share the messages of the topic: one of them receives the order
        received = CompletableFuture.anyOf(
            self.order_subscribers.account_orders.receive(), self.group_order_controller.topic.receive()
        ).get(1, TimeUnit.MINUTES)
        assert received.getValue().getProductId() == "AB1234"

        self.order_publishers.publisher.publish(Order(2, "marge", "XY9999")).join()
        self.order_controller.topic.publish(Order(3, "bart", "AB1234")).join()
        # the filtered subscribers only receive the orders of product AB1234
        assert receive(self.order_subscribers.filtered_orders).getOrderId() == 3
        assert receive(self.filtered_order_controller.topic).getOrderId() == 3
        assert receive(self.order_subscribers.orders).getOrderId() == 2
        assert receive(self.order_subscribers.orders).getOrderId() == 3

    @Test
    def test_publish_and_subscribe_in_session(self):
        self.order_publishers.customer_orders.publish(Order(4, "lisa", "AB1234")).join()
        assert receive(self.order_subscribers.customer_orders).getOrderId() == 4
