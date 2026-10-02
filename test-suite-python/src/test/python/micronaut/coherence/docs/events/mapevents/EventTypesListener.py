# tag::imports[]
from typing import Annotated

from com.tangosol.util import MapEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Deleted, Inserted, MapName
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MapEventsTest")
@Singleton
class EventTypesListener:

    def __init__(self):
        self.events: dict[str, list] = {}

    # tag::inserted[]
    @CoherenceEventListener
    def on_inserted(self, event: Annotated[MapEvent[str, str],
                                           MapName("test"),
                                           Inserted]) -> None:        # <1>
        self.record("on_inserted", event)  # process the event
    # end::inserted[]

    # tag::insertedDeleted[]
    @CoherenceEventListener
    def on_inserted_or_deleted(self, event: Annotated[MapEvent[str, str],
                                                      MapName("test"),
                                                      Inserted, Deleted]) -> None:       # <1>
        self.record("on_inserted_or_deleted", event)  # process the event
    # end::insertedDeleted[]

    # tag::all[]
    @CoherenceEventListener
    def on_event(self, event: Annotated[MapEvent[str, str], MapName("test")]) -> None:
        self.record("on_event", event)  # process the event
    # end::all[]

    def record(self, listener: str, event) -> None:
        self.events.setdefault(listener, []).append(event)

    def get_events(self, listener: str) -> list:
        return self.events.get(listener, [])
