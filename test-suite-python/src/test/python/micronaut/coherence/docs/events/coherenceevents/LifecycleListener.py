# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.application import LifecycleEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import Activated, CoherenceEventListener, Disposing
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class LifecycleListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: LifecycleEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::types[]
    @CoherenceEventListener
    def on_activated_or_disposing(self, event: Annotated[LifecycleEvent, Activated, Disposing]) -> None:
        self.record("onActivatedOrDisposing", event)  # process the event
    # end::types[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}")
