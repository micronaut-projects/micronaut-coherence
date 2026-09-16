# tag::imports[]
from typing import Annotated

from com.tangosol.util import MapEvent
from micronaut.coherence.annotation import CoherenceEventListener, Inserted, MapName
from micronaut.coherence.examples.model import Person
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MapEventsTest")
# tag::clazz[]
@Controller                                                    # <1>
class PersonController:

    def __init__(self):
        self.new_people: list[Person] = []

    @CoherenceEventListener                                    # <2>
    def on_new_person(self, event: Annotated[MapEvent[str, Person],
                                             MapName("people"),     # <3>
                                             Inserted]) -> None:   # <4>
        self.new_people.append(event.getNewValue())            # process the event
# end::clazz[]
