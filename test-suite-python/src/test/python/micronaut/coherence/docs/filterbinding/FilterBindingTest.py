from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Person
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test

from .AdultMalesView import AdultMalesView


@Property(name="spec.name", value="FilterBindingTest")
@MicronautTest
class FilterBindingTest:
    people: Annotated[NamedMap[str, Person], Inject, Name("people")]
    view: Annotated[AdultMalesView, Inject]

    @Disabled("TODO(python): a Java Map injected into a Python bean is coerced to a copy (PythonCoercion.coerceToContext), the NamedMap API is lost")
    @Test
    def test_custom_filter_binding(self):
        self.people.put("homer", Person("Homer", "Simpson", 39, "male"))
        self.people.put("marge", Person("Marge", "Simpson", 36, "female"))
        self.people.put("bart", Person("Bart", "Simpson", 10, "male"))
        self.people.put("lisa", Person("Lisa", "Simpson", 8, "female"))

        assert list(self.view.adult_males.keySet()) == ["homer"]
        assert list(self.view.adult_females.keySet()) == ["marge"]
