# tag::imports[]
from typing import Annotated

from com.tangosol.net import Coherence
from com.tangosol.net.events import SessionLifecycleEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Name, Started, Stopped
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class SessionLifecycleListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: SessionLifecycleEvent) -> None:
        self.record("onEvent", event)  # process the event
    # end::all[]

    # tag::types[]
    @CoherenceEventListener
    def on_started_or_stopped(self, event: Annotated[SessionLifecycleEvent, Started, Stopped]) -> None:
        self.record("onStartedOrStopped", event)  # process the event
    # end::types[]

    # tag::name[]
    @CoherenceEventListener
    def on_customers_event(self, event: Annotated[SessionLifecycleEvent, Name("Customers")]) -> None:
        self.record("onCustomersEvent", event)  # process the event
    # end::name[]

    # tag::defaultName[]
    @CoherenceEventListener
    def on_default_event(self, event: Annotated[SessionLifecycleEvent, Name(Coherence.DEFAULT_NAME)]) -> None:
        self.record("onDefaultEvent", event)  # process the event
    # end::defaultName[]

    def record(self, listener: str, event) -> None:
        self.events.append(f"{listener}:{event.getType()}:{event.getSession().getName()}")
