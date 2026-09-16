from typing import Annotated

from com.tangosol.net.cache import ContinuousQueryCache
from jakarta.inject import Inject
from micronaut.coherence.examples.model import Person
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test

from .AsyncPeopleService import AsyncPeopleService
from .CatalogController import CatalogController
from .CatalogService import CatalogService
from .PeopleService import PeopleService
from .PeopleViews import PeopleViews
from .SomeBean import SomeBean


@Property(name="spec.name", value="NamedMapInjectionTest")
@MicronautTest
class NamedMapInjectionTest:
    people_service: Annotated[PeopleService, Inject]
    some_bean: Annotated[SomeBean, Inject]
    catalog_service: Annotated[CatalogService, Inject]
    catalog_controller: Annotated[CatalogController, Inject]
    async_people_service: Annotated[AsyncPeopleService, Inject]
    people_views: Annotated[PeopleViews, Inject]

    @Disabled("TODO(python): a Java Map injected into a Python bean is coerced to a copy (PythonCoercion.coerceToContext), the NamedMap API is lost")
    @Test
    def test_inject_named_map(self):
        assert self.people_service.people.getName() == "people"
        assert self.people_service.map.getName() == "people"
        assert self.some_bean.map.getName() == "people"
        assert self.async_people_service.map.getNamedMap().getName() == "people"

        self.people_service.people.put("homer", Person("Homer", "Simpson", 39, "male"))
        assert self.people_service.map.get("homer").getFirstName() == "Homer"
        assert self.some_bean.map.get("homer").getFirstName() == "Homer"
        assert self.async_people_service.map.get("homer").join().getFirstName() == "Homer"

    @Disabled("TODO(python): a Java Map injected into a Python bean is coerced to a copy (PythonCoercion.coerceToContext), the NamedMap API is lost")
    @Test
    def test_inject_named_map_from_session(self):
        products = self.catalog_service.map
        assert products.getName() == "products"
        assert self.catalog_controller.products.getName() == "products"
        assert products.getService().getBackingMapManager().getCacheFactory().getScopeName() == "Catalog"
        assert not self.people_service.people.getService().equals(products.getService())

    @Disabled("TODO(python): a Java Map injected into a Python bean is coerced to a copy (PythonCoercion.coerceToContext), the NamedMap API is lost")
    @Test
    def test_inject_views(self):
        people = self.people_service.people
        people.put("homer", Person("Homer", "Simpson", 39, "male"))
        people.put("marge", Person("Marge", "Simpson", 36, "female"))
        people.put("maggie", Person("Maggie", "Simpson", 1, "female"))
        people.put("ned", Person("Ned", "Flanders", 60, "male"))

        assert isinstance(self.people_views.map, ContinuousQueryCache)
        assert self.people_views.map.size() == 4

        assert self.people_views.simpsons.size() == 3
        assert all(p.getLastName() == "Simpson" for p in self.people_views.simpsons.values())

        assert list(self.people_views.adult_male_simpsons.keySet()) == ["homer"]

        assert self.people_views.ages.size() == 4
        assert sorted(self.people_views.ages.values()) == [1, 36, 39, 60]
