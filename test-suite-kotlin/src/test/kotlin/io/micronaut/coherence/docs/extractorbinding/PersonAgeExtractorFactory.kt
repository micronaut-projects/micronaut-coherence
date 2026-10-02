package io.micronaut.coherence.docs.extractorbinding

// tag::imports[]
import com.tangosol.util.Extractors
import com.tangosol.util.ValueExtractor
import io.micronaut.coherence.ExtractorFactory
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ExtractorBindingTest")
// tag::clazz[]
@PersonAge     // <1>
@Singleton     // <2>
class PersonAgeExtractorFactory : ExtractorFactory<PersonAge, Person, Int> {
    override fun create(annotation: PersonAge): ValueExtractor<Person, Int> {       // <3>
        return Extractors.extract("age")
    }
}
// end::clazz[]
