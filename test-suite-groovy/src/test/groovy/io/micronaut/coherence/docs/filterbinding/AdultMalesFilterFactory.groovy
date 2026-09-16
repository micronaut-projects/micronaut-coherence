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
class AdultMalesFilterFactory implements FilterFactory<AdultMales, Person> {
    @Override
    Filter<Person> create(AdultMales annotation) {       // <3>
        Filter<Person> male = Filters.equal("gender", "male")
        Filter<Person> adult = Filters.greaterEqual(Extractors.extract("age"), 18)
        return Filters.all(male, adult)
    }
}
// end::clazz[]
