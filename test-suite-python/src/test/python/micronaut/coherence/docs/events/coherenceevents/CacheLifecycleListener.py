# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.partition.cache import CacheLifecycleEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Created, Destroyed, MapName, ScopeName, ServiceName
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class CacheLifecycleListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: CacheLifecycleEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::types[]
    @CoherenceEventListener
    def on_created_or_destroyed(self, event: Annotated[CacheLifecycleEvent, Created, Destroyed]) -> None:
        self.record("onCreatedOrDestroyed", event)  # process the event
    # end::types[]

    # tag::mapName[]
    @CoherenceEventListener
    def on_orders_event(self, event: Annotated[CacheLifecycleEvent, MapName("orders")]) -> None:
        self.record("onOrdersEvent", event)  # process the event
    # end::mapName[]

    # tag::serviceName[]
    @CoherenceEventListener
    def on_storage_service_event(self, event: Annotated[CacheLifecycleEvent, ServiceName("StorageService")]) -> None:
        self.record("onStorageServiceEvent", event)  # process the event
    # end::serviceName[]

    # tag::sessionName[]
    @CoherenceEventListener
    def on_back_end_event(self, event: Annotated[CacheLifecycleEvent, ScopeName("BackEnd")]) -> None:
        self.record("onBackEndEvent", event)  # process the event
    # end::sessionName[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}:{event.getCacheName()}")
