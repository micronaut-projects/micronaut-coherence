# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.partition.cache import EntryEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Inserted, MapName, Removed, ScopeName, ServiceName
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class EntryListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: EntryEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::types[]
    @CoherenceEventListener
    def on_inserted_or_removed(self, event: Annotated[EntryEvent, Inserted, Removed]) -> None:
        self.record("onInsertedOrRemoved", event)  # process the event
    # end::types[]

    # tag::mapName[]
    @CoherenceEventListener
    def on_orders_event(self, event: Annotated[EntryEvent, MapName("orders")]) -> None:
        self.record("onOrdersEvent", event)  # process the event
    # end::mapName[]

    # tag::serviceName[]
    @CoherenceEventListener
    def on_storage_service_event(self, event: Annotated[EntryEvent, ServiceName("StorageService")]) -> None:
        self.record("onStorageServiceEvent", event)  # process the event
    # end::serviceName[]

    # tag::sessionName[]
    @CoherenceEventListener
    def on_back_end_event(self, event: Annotated[EntryEvent, ScopeName("BackEnd")]) -> None:
        self.record("onBackEndEvent", event)  # process the event
    # end::sessionName[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}:{event.getKey()}")
