package io.micronaut.coherence.docs.injection.maps

import com.tangosol.net.cache.ContinuousQueryCache
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "spec.name", value = "NamedMapInjectionTest")
@MicronautTest
class NamedMapInjectionSpec extends Specification {

    @Inject
    PeopleService peopleService

    @Inject
    SomeBean someBean

    @Inject
    CatalogService catalogService

    @Inject
    CatalogController catalogController

    @Inject
    AsyncPeopleService asyncPeopleService

    @Inject
    PeopleViews peopleViews

    void "test inject named map"() {
        expect:
        peopleService.people.name == "people"
        peopleService.map.name == "people"
        someBean.map.name == "people"
        asyncPeopleService.map.namedMap.name == "people"

        when:
        peopleService.people.put("homer", new Person("Homer", "Simpson", 39, "male"))

        then:
        peopleService.map.get("homer").firstName == "Homer"
        someBean.map.get("homer").firstName == "Homer"
        asyncPeopleService.map.get("homer").join().firstName == "Homer"
    }

    void "test inject named map from session"() {
        expect:
        catalogService.map.name == "products"
        catalogController.products.name == "products"
        catalogService.map.service.backingMapManager.cacheFactory.scopeName == "Catalog"
        !peopleService.people.service.is(catalogService.map.service)
    }

    void "test inject views"() {
        given:
        def people = peopleService.people
        people.put("homer", new Person("Homer", "Simpson", 39, "male"))
        people.put("marge", new Person("Marge", "Simpson", 36, "female"))
        people.put("maggie", new Person("Maggie", "Simpson", 1, "female"))
        people.put("ned", new Person("Ned", "Flanders", 60, "male"))

        expect:
        peopleViews.map instanceof ContinuousQueryCache
        peopleViews.map.size() == 4
        peopleViews.simpsons.size() == 3
        peopleViews.simpsons.values().every { it.lastName == "Simpson" }
        peopleViews.adultMaleSimpsons.keySet() == ["homer"] as Set
        peopleViews.ages.size() == 4
        peopleViews.ages.values().sort() == [1, 36, 39, 60]
    }
}
