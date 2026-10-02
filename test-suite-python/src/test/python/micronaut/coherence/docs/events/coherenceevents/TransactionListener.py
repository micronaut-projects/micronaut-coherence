# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.partition import TransactionEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Committed, ServiceName
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class TransactionListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: TransactionEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::types[]
    @CoherenceEventListener
    def on_committed(self, event: Annotated[TransactionEvent, Committed]) -> None:
        self.record("onCommitted", event)  # process the event
    # end::types[]

    # tag::serviceName[]
    @CoherenceEventListener
    def on_storage_service_event(self, event: Annotated[TransactionEvent, ServiceName("StorageService")]) -> None:
        self.record("onStorageServiceEvent", event)  # process the event
    # end::serviceName[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}")
