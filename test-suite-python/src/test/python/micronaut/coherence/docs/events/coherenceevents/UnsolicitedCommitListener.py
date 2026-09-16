# tag::imports[]
from typing import Annotated

from com.tangosol.net.events.partition import UnsolicitedCommitEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CoherenceEventsTest")
@Singleton
class UnsolicitedCommitListener:

    def __init__(self):
        self.events: list[str] = []

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: UnsolicitedCommitEvent) -> None:
        self.events.append(str(event.getType()))  # process the event
    # end::all[]
