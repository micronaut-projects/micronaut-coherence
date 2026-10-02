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

@Requires(property = "spec.name", value = "FilterBindingTest")
// tag::clazz[]
@Adults("")
@Singleton
class AdultsFilterFactory implements FilterFactory<Adults, Person> {
    @Override
    Filter<Person> create(Adults annotation) {       // <1>
        Filter<Person> gender = Filters.equal("gender", annotation.value())
        Filter<Person> adult = Filters.greaterEqual(Extractors.extract("age"), 18)
        return Filters.all(gender, adult)
    }
}
// end::clazz[]
