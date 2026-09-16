package io.micronaut.coherence.docs.filterbinding

// tag::imports[]
import com.tangosol.util.Extractors
import com.tangosol.util.Filter
import com.tangosol.util.Filters
import io.micronaut.coherence.FilterFactory
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", pattern = "NamedMapInjectionTest|FilterBindingTest")
// tag::clazz[]
@AdultMales    // <1>
@Singleton     // <2>
class AdultMalesFilterFactory : FilterFactory<AdultMales, Person> {
    override fun create(annotation: AdultMales): Filter<Person> {       // <3>
        val male = Filters.equal<Person, String>("gender", "male")
        val adult = Filters.greaterEqual(Extractors.extract<Person, Int>("age"), 18)
        @Suppress("UNCHECKED_CAST")
        return Filters.all(male, adult) as Filter<Person>
    }
}
// end::clazz[]
