package io.micronaut.coherence.docs.injection.maps;

import com.tangosol.net.NamedMap;
import com.tangosol.net.cache.ContinuousQueryCache;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "NamedMapInjectionTest")
@MicronautTest
class NamedMapInjectionTest {

    @Inject
    PeopleService peopleService;

    @Inject
    SomeBean someBean;

    @Inject
    CatalogService catalogService;

    @Inject
    CatalogController catalogController;

    @Inject
    AsyncPeopleService asyncPeopleService;

    @Inject
    PeopleViews peopleViews;

    @Test
    void testInjectNamedMap() {
        assertEquals("people", peopleService.people.getName());
        assertEquals("people", peopleService.map.getName());
        assertEquals("people", someBean.getMap().getName());
        assertEquals("people", asyncPeopleService.map.getNamedMap().getName());

        peopleService.people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        assertEquals("Homer", peopleService.map.get("homer").getFirstName());
        assertEquals("Homer", someBean.getMap().get("homer").getFirstName());
        assertEquals("Homer", asyncPeopleService.map.get("homer").join().getFirstName());
    }

    @Test
    void testInjectNamedMapFromSession() {
        NamedMap<String, ?> products = catalogService.map;
        assertEquals("products", products.getName());
        assertEquals("products", catalogController.getProducts().getName());
        assertEquals("Catalog", products.getService().getBackingMapManager().getCacheFactory().getScopeName());
        assertNotEquals(peopleService.people.getService(), products.getService());
    }

    @Test
    void testInjectViews() {
        NamedMap<String, Person> people = peopleService.people;
        people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        people.put("marge", new Person("Marge", "Simpson", 36, "female"));
        people.put("maggie", new Person("Maggie", "Simpson", 1, "female"));
        people.put("ned", new Person("Ned", "Flanders", 60, "male"));

        assertInstanceOf(ContinuousQueryCache.class, peopleViews.map);
        assertEquals(4, peopleViews.map.size());

        assertEquals(3, peopleViews.simpsons.size());
        assertTrue(peopleViews.simpsons.values().stream().allMatch(p -> "Simpson".equals(p.getLastName())));

        assertEquals(Set.of("homer"), peopleViews.adultMaleSimpsons.keySet());

        assertEquals(4, peopleViews.ages.size());
        assertEquals(List.of(1, 36, 39, 60), peopleViews.ages.values().stream().sorted().toList());
    }
}
