package io.micronaut.coherence.docs.filterbinding

import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "spec.name", value = "FilterBindingTest")
@MicronautTest
class FilterBindingSpec extends Specification {

    @Inject
    @Name("people")
    NamedMap<String, Person> people

    @Inject
    AdultMalesView view

    void "test custom filter binding"() {
        when:
        people.put("homer", new Person("Homer", "Simpson", 39, "male"))
        people.put("marge", new Person("Marge", "Simpson", 36, "female"))
        people.put("bart", new Person("Bart", "Simpson", 10, "male"))
        people.put("lisa", new Person("Lisa", "Simpson", 8, "female"))

        then:
        view.adultMales.keySet() == ["homer"] as Set
        view.adultFemales.keySet() == ["marge"] as Set
    }
}
