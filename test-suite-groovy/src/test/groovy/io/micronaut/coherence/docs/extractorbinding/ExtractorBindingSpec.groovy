package io.micronaut.coherence.docs.extractorbinding

import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "spec.name", value = "ExtractorBindingTest")
@MicronautTest
class ExtractorBindingSpec extends Specification {

    @Inject
    @Name("people")
    NamedMap<String, Person> people

    @Inject
    PersonAgeView view

    void "test custom extractor binding"() {
        when:
        people.put("homer", new Person("Homer", "Simpson", 39, "male"))
        people.put("bart", new Person("Bart", "Simpson", 10, "male"))

        then:
        view.ages.size() == 2
        view.ages.get("homer") == 39
        view.ages.get("bart") == 10
    }
}
