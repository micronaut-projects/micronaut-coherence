# tag::imports[]
from com.tangosol.util import MapEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, MapName, WhereFilter
from micronaut.coherence.examples.model import Person
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MapEventsTest")
@Singleton
class FilteredListener:

    def __init__(self):
        self.adults: list = []

    # tag::filtered[]
    @WhereFilter("age >= 18")     # <1>
    @CoherenceEventListener
    @MapName("people")
    def on_adult(self, people: MapEvent[str, Person]) -> None:
        self.adults.append(people)  # process event...
    # end::filtered[]
