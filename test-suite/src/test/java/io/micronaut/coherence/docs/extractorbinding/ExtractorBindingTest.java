package io.micronaut.coherence.docs.extractorbinding;

import com.tangosol.net.NamedMap;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "ExtractorBindingTest")
@MicronautTest
class ExtractorBindingTest {

    @Inject
    @Name("people")
    NamedMap<String, Person> people;

    @Inject
    PersonAgeView view;

    @Test
    void testCustomExtractorBinding() {
        people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        people.put("bart", new Person("Bart", "Simpson", 10, "male"));

        assertEquals(2, view.ages.size());
        assertEquals(39, view.ages.get("homer"));
        assertEquals(10, view.ages.get("bart"));
    }
}
