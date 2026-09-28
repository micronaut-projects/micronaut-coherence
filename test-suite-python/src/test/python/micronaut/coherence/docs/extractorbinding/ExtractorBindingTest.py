from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Person
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .PersonAgeView import PersonAgeView


@Property(name="spec.name", value="ExtractorBindingTest")
@MicronautTest
class ExtractorBindingTest:
    people: Annotated[NamedMap[str, Person], Inject, Name("people")]
    view: Annotated[PersonAgeView, Inject]

    @Test
    def test_custom_extractor_binding(self):
        self.people.put("homer", Person("Homer", "Simpson", 39, "male"))
        self.people.put("bart", Person("Bart", "Simpson", 10, "male"))

        # The view is read through its Java entrySet() rather than with dict(self.view.ages): the Python
        # mapping protocol reads a Java Map with Map.getOrDefault(), and a Coherence view answers that
        # from the cache it is a view of, so it returns the untransformed Person. That is not specific to
        # Python - see #1066 - and once #1067 is merged into 7.1.x this becomes
        #     assert dict(self.view.ages) == {"homer": 39, "bart": 10}
        ages = {entry.getKey(): entry.getValue() for entry in self.view.ages.entrySet()}
        assert ages == {"homer": 39, "bart": 10}
