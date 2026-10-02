package io.micronaut.coherence.docs.filterbinding

// tag::imports[]
import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.View
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "FilterBindingTest")
@Singleton
class AdultMalesView {

    // tag::inject[]
    @Inject
    @View               // <1>
    @AdultMales         // <2>
    @Name("people")     // <3>
    protected NamedMap<String, Person> adultMales
    // end::inject[]

    // tag::adults[]
    @Inject
    @View
    @Adults("female")
    @Name("people")
    protected NamedMap<String, Person> adultFemales
    // end::adults[]
}
