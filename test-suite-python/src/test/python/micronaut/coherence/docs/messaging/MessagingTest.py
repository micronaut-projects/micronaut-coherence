from time import sleep
from typing import Annotated

from com.tangosol.net import Session
from jakarta.inject import Inject
from java.util.concurrent import TimeUnit
from micronaut.coherence.examples import MessagingHelper
from micronaut.coherence.examples.model import Book, Product
from micronaut.context import ApplicationContext
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Test
from reactor.core.publisher import Flux, Mono

from .AsyncCommitListener import AsyncCommitListener
from .BookClient import BookClient
from .ManualCommitListener import ManualCommitListener
from .ProductElementListener import ProductElementListener
from .ProductClient import ProductClient
from .ProductListener import ProductListener
from .SyncCommitListener import SyncCommitListener


def await_value(actual, expected) -> None:
    for _ in range(600):
        if actual() == expected:
            break
        sleep(0.05)
    assert actual() == expected


def receive(subscriber):
    return subscriber.receive().get(1, TimeUnit.MINUTES).getValue()


@Property(name="spec.name", value="MessagingTest")
@MicronautTest
class MessagingTest:
    application_context: Annotated[ApplicationContext, Inject]
    session: Annotated[Session, Inject]
    book_client: Annotated[BookClient, Inject]
    product_listener: Annotated[ProductListener, Inject]
    product_element_listener: Annotated[ProductElementListener, Inject]
    sync_commit_listener: Annotated[SyncCommitListener, Inject]
    async_commit_listener: Annotated[AsyncCommitListener, Inject]
    manual_commit_listener: Annotated[ManualCommitListener, Inject]

    @BeforeEach
    def await_subscribers(self):
        MessagingHelper.awaitSubscribed(self.application_context)

    @Test
    def test_publisher_and_listener(self):
        # tag::usage[]
        client = self.application_context.getBean(ProductClient)
        client.send_product("Blue Trainers")
        # end::usage[]
        client.send_product_to("my-products", "Red Trainers")

        await_value(lambda: self.product_listener.products, ["Blue Trainers", "Red Trainers"])

    @Test
    def test_reactive_publisher(self):
        subscriber = self.session.getTopic("books").createSubscriber()

        sent = Mono.from_(self.book_client.send_book(Mono.just(Book("Micronaut in Action", 400)))).block()
        assert sent.getTitle() == "Micronaut in Action"
        assert receive(subscriber).getTitle() == "Micronaut in Action"

        books = Flux.from_(self.book_client.send_books(Flux.just(Book("One", 1), Book("Two", 2)))).collectList().block()
        assert [b.getTitle() for b in books] == ["One", "Two"]
        assert receive(subscriber).getTitle() == "One"
        assert receive(subscriber).getTitle() == "Two"

    @Test
    def test_commit_strategies(self):
        publisher = self.session.getTopic("products").createPublisher()
        try:
            publisher.publish(Product("Trainers", "Acme", 3)).get(1, TimeUnit.MINUTES)
        finally:
            publisher.close()
        await_value(lambda: len(self.product_element_listener.elements), 1)
        assert self.product_element_listener.elements[0].getValue().getName() == "Trainers"
        await_value(lambda: len(self.sync_commit_listener.products), 1)
        await_value(lambda: len(self.async_commit_listener.products), 1)
        await_value(lambda: len(self.manual_commit_listener.products), 1)

    @Test
    def test_forwarding(self):
        quantities = self.session.getTopic("product-quantities").createSubscriber()
        publisher = self.session.getTopic("awesome-products").createPublisher()
        try:
            publisher.publish(Product("Trainers", "Acme", 7)).get(1, TimeUnit.MINUTES)
        finally:
            publisher.close()
        # both listeners forward the quantity of the product
        assert [receive(quantities), receive(quantities)] == [7, 7]
