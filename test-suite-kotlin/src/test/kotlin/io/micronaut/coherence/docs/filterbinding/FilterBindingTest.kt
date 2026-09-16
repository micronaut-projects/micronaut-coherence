package io.micronaut.coherence.docs.filterbinding

import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "FilterBindingTest")
@MicronautTest
class FilterBindingTest {

    @Inject
    @Name("people")
    lateinit var people: NamedMap<String, Person>

    @Inject
    lateinit var view: AdultMalesView

    @Test
    fun testCustomFilterBinding() {
        people["homer"] = Person("Homer", "Simpson", 39, "male")
        people["marge"] = Person("Marge", "Simpson", 36, "female")
        people["bart"] = Person("Bart", "Simpson", 10, "male")
        people["lisa"] = Person("Lisa", "Simpson", 8, "female")

        assertEquals(setOf("homer"), view.adultMales.keys)
        assertEquals(setOf("marge"), view.adultFemales.keys)
    }
}
