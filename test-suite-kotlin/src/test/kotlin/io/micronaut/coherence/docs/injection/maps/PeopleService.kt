package io.micronaut.coherence.docs.injection.maps

// tag::imports[]
import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
@Singleton
class PeopleService {

    // tag::inject[]
    @Inject
    lateinit var people: NamedMap<String, Person>
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("people")
    lateinit var map: NamedMap<String, Person>
    // end::name[]
}
