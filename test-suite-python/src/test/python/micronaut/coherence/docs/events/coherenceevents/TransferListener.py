# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.partition import TransferEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import Arrived, CoherenceEventListener, Lost, ServiceName
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class TransferListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: TransferEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::arrived[]
    @CoherenceEventListener
    def on_arrived(self, event: Annotated[TransferEvent, Arrived]) -> None:
        restored = event.getRemoteMember().equals(event.getLocalMember())
        self.record("onRestored" if restored else "onArrived", event)  # process the event
    # end::arrived[]

    # tag::types[]
    @CoherenceEventListener
    def on_lost(self, event: Annotated[TransferEvent, Lost]) -> None:
        self.record("onLost", event)  # process the event
    # end::types[]

    # tag::serviceName[]
    @CoherenceEventListener
    def on_storage_service_event(self, event: Annotated[TransferEvent, ServiceName("StorageService")]) -> None:
        self.record("onStorageServiceEvent", event)  # process the event
    # end::serviceName[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}")
