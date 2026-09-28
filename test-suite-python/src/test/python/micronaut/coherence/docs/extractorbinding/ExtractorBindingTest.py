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

        # TODO(python): the view is read through its Java entrySet(): the Python mapping access to the injected
        # ContinuousQueryCache (ages["homer"], ages.get("homer"), dict(ages)) returns the untransformed Person
        # although entrySet() and values() return the extracted ages. Re-checked against core 5.2.9.
        ages = {entry.getKey(): entry.getValue() for entry in self.view.ages.entrySet()}
        assert ages == {"homer": 39, "bart": 10}
