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
class PersonAgeExtractorFactory implements ExtractorFactory<PersonAge, Person, Integer> {
    @Override
    ValueExtractor<Person, Integer> create(PersonAge annotation) {       // <3>
        return Extractors.extract("age")
    }
}
// end::clazz[]
