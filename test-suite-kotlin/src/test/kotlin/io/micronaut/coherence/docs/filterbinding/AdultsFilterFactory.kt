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
class AdultsFilterFactory : FilterFactory<Adults, Person> {
    override fun create(annotation: Adults): Filter<Person> {       // <1>
        val gender = Filters.equal<Person, String>("gender", annotation.value)
        val adult = Filters.greaterEqual(Extractors.extract<Person, Int>("age"), 18)
        @Suppress("UNCHECKED_CAST")
        return Filters.all(gender, adult) as Filter<Person>
    }
}
// end::clazz[]
