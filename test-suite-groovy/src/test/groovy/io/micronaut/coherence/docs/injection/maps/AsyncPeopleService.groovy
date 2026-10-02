package io.micronaut.coherence.docs.injection.maps

// tag::imports[]
import com.tangosol.net.AsyncNamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
@Singleton
class AsyncPeopleService {

    // tag::inject[]
    @Inject
    @Name("people")
    protected AsyncNamedMap<String, Person> map
    // end::inject[]
}
