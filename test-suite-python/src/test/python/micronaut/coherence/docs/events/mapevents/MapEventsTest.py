from time import sleep
from typing import Annotated

from com.tangosol.net import Session
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples import EventsHelper
from micronaut.coherence.examples.model import Order, Person
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .EventTypesListener import EventTypesListener
from .FilteredListener import FilteredListener
from .MapEventListeners import MapEventListeners
from .PersonController import PersonController
from .TransformedListener import TransformedListener


def await_count(actual, expected: int) -> None:
    for _ in range(600):
        if actual() == expected:
            break
        sleep(0.05)
    assert actual() == expected


@Property(name="spec.name", value="MapEventsTest")
@MicronautTest
class MapEventsTest:
    session: Annotated[Session, Inject]
    customer_session: Annotated[Session, Inject, Name("Customer")]
    catalog_session: Annotated[Session, Inject, Name("Catalog")]
    person_controller: Annotated[PersonController, Inject]
    listeners: Annotated[MapEventListeners, Inject]
    event_types_listener: Annotated[EventTypesListener, Inject]
    filtered_listener: Annotated[FilteredListener, Inject]
    transformed_listener: Annotated[TransformedListener, Inject]

    @Test
    def test_map_events(self):
        people = self.session.getMap("people")
        foo = self.session.getMap("foo")
        test = self.session.getMap("test")
        orders = self.session.getMap("orders")
        customer_orders = self.customer_session.getMap("orders")
        catalog_orders = self.catalog_session.getMap("orders")
        # the listeners are registered asynchronously when the caches are created
        EventsHelper.awaitListeners(people, 2)
        EventsHelper.awaitListeners(foo, 1)
        EventsHelper.awaitListeners(test, 1)
        EventsHelper.awaitListeners(orders, 1)
        EventsHelper.awaitListeners(customer_orders, 1)
        EventsHelper.awaitListeners(catalog_orders, 1)

        people.put("homer", Person("Homer", "Simpson", 39, "male"))
        people.put("bart", Person("Bart", "Simpson", 10, "male"))
        foo.put("a", "b")
        test.put("a", "1")
        test.put("a", "2")
        test.remove("a")
        orders.put("1", Order(1, "homer", "AB1234"))
        customer_orders.put("2", Order(2, "marge", "AB1234"))
        catalog_orders.put("3", Order(3, "lisa", "AB1234"))

        # events.adoc / mapEvents.adoc
        await_count(lambda: len(self.person_controller.new_people), 2)
        await_count(lambda: len(self.listeners.get_events("on_foo_map_event")), 1)
        await_count(lambda: len(self.listeners.get_events("on_foo_cache_event")), 1)
        await_count(lambda: len(self.listeners.get_events("on_storage_foo_event")), 1)
        await_count(lambda: len(self.listeners.get_events("on_orders_event")), 3)
        await_count(lambda: len(self.listeners.get_events("on_customer_orders_event")), 1)
        await_count(lambda: len(self.listeners.get_events("on_customer_event")), 1)
        await_count(lambda: len(self.listeners.get_events("on_customer_orders")), 1)
        await_count(lambda: len(self.listeners.get_events("on_catalog_orders")), 1)
        assert self.listeners.get_events("on_customer_orders")[0].getKey() == "2"
        assert self.listeners.get_events("on_catalog_orders")[0].getKey() == "3"
        assert len(self.listeners.get_events("on_event")) >= 9
        assert len(self.listeners.get_events("on_storage_event")) >= 7

        # eventTypes.adoc
        await_count(lambda: len(self.event_types_listener.get_events("on_inserted")), 1)
        await_count(lambda: len(self.event_types_listener.get_events("on_inserted_or_deleted")), 2)
        await_count(lambda: len(self.event_types_listener.get_events("on_event")), 3)

        # filteringEvents.adoc
        await_count(lambda: len(self.filtered_listener.adults), 1)
        assert self.filtered_listener.adults[0].getKey() == "homer"

        # transformingEvents.adoc
        # the "orders" maps of all sessions are observed
        await_count(lambda: len(self.transformed_listener.customer_ids), 3)
        assert set(self.transformed_listener.customer_ids) == {"homer", "marge", "lisa"}
        await_count(lambda: len(self.transformed_listener.new_orders), 3)
        assert self.transformed_listener.new_orders == {1: "homer", 2: "marge", 3: "lisa"}
