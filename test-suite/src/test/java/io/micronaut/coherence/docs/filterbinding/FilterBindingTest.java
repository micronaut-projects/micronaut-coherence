package io.micronaut.coherence.docs.filterbinding;

import com.tangosol.net.NamedMap;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "FilterBindingTest")
@MicronautTest
class FilterBindingTest {

    @Inject
    @Name("people")
    NamedMap<String, Person> people;

    @Inject
    AdultMalesView view;

    @Test
    void testCustomFilterBinding() {
        people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        people.put("marge", new Person("Marge", "Simpson", 36, "female"));
        people.put("bart", new Person("Bart", "Simpson", 10, "male"));
        people.put("lisa", new Person("Lisa", "Simpson", 8, "female"));

        assertEquals(Set.of("homer"), view.adultMales.keySet());
        assertEquals(Set.of("marge"), view.adultFemales.keySet());
    }
}
