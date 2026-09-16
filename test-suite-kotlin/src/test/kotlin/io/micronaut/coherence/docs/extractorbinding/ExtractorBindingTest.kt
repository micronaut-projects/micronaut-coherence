package io.micronaut.coherence.docs.extractorbinding

import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "ExtractorBindingTest")
@MicronautTest
class ExtractorBindingTest {

    @Inject
    @Name("people")
    lateinit var people: NamedMap<String, Person>

    @Inject
    lateinit var view: PersonAgeView

    @Test
    fun testCustomExtractorBinding() {
        people["homer"] = Person("Homer", "Simpson", 39, "male")
        people["bart"] = Person("Bart", "Simpson", 10, "male")

        assertEquals(2, view.ages.size)
        assertEquals(39, view.ages["homer"])
        assertEquals(10, view.ages["bart"])
    }
}
