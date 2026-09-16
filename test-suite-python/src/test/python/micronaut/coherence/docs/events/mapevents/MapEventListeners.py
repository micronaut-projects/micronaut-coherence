# tag::imports[]
from typing import Annotated

from com.tangosol.util import MapEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CacheName, CoherenceEventListener, MapName, ServiceName, SessionName
from micronaut.coherence.examples.model import Order
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MapEventsTest")
@Singleton
class MapEventListeners:

    def __init__(self):
        self.events: dict[str, list] = {}

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: MapEvent[str, str]) -> None:
        self.record("on_event", event)  # process the event
    # end::all[]

    # tag::mapName[]
    @CoherenceEventListener
    def on_foo_map_event(self, event: Annotated[MapEvent[str, str], MapName("foo")]) -> None:  # <1>
        self.record("on_foo_map_event", event)  # process the event
    # end::mapName[]

    # tag::cacheName[]
    @CoherenceEventListener
    def on_foo_cache_event(self, event: Annotated[MapEvent[str, str], CacheName("foo")]) -> None:  # <1>
        self.record("on_foo_cache_event", event)  # process the event
    # end::cacheName[]

    # tag::serviceName[]
    @CoherenceEventListener
    def on_storage_foo_event(self, event: Annotated[MapEvent[str, str],
                                                    MapName("foo"),
                                                    ServiceName("StorageService")]) -> None:  # <1>
        self.record("on_storage_foo_event", event)  # process the event
    # end::serviceName[]

    # tag::serviceOnly[]
    @CoherenceEventListener
    def on_storage_event(self, event: Annotated[MapEvent[str, str], ServiceName("StorageService")]) -> None:  # <1>
        self.record("on_storage_event", event)  # process the event
    # end::serviceOnly[]

    # tag::allSessions[]
    @CoherenceEventListener
    def on_orders_event(self, event: Annotated[MapEvent[str, Order], MapName("orders")]) -> None:  # <1>
        self.record("on_orders_event", event)  # process the event
    # end::allSessions[]

    # tag::sessionName[]
    @CoherenceEventListener
    def on_customer_orders_event(self, event: Annotated[MapEvent[str, Order],
                                                        MapName("orders"),
                                                        SessionName("Customer")]) -> None:  # <1>
        self.record("on_customer_orders_event", event)  # process the event
    # end::sessionName[]

    # tag::sessionOnly[]
    @CoherenceEventListener
    def on_customer_event(self, event: Annotated[MapEvent[str, Order], SessionName("Customer")]) -> None:  # <1>
        self.record("on_customer_event", event)  # process the event
    # end::sessionOnly[]

    # tag::routing[]
    @CoherenceEventListener
    def on_customer_orders(self, event: Annotated[MapEvent[str, Order],
                                                  SessionName("Customer"),  # <1>
                                                  MapName("orders")]) -> None:
        self.record("on_customer_orders", event)  # process the event

    @CoherenceEventListener
    def on_catalog_orders(self, event: Annotated[MapEvent[str, Order],
                                                 SessionName("Catalog"),   # <2>
                                                 MapName("orders")]) -> None:
        self.record("on_catalog_orders", event)  # process the event
    # end::routing[]

    def record(self, listener: str, event) -> None:
        self.events.setdefault(listener, []).append(event)

    def get_events(self, listener: str) -> list:
        return self.events.get(listener, [])
