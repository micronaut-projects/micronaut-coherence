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

        assert self.view.ages.size() == 2
        assert self.view.ages.get("homer") == 39
        assert self.view.ages.get("bart") == 10
