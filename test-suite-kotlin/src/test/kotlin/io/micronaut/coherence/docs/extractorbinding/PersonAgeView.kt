package io.micronaut.coherence.docs.extractorbinding

// tag::imports[]
import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.View
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ExtractorBindingTest")
@Singleton
class PersonAgeView {

    // tag::inject[]
    @Inject
    @View               // <1>
    @PersonAge          // <2>
    @Name("people")     // <3>
    lateinit var ages: NamedMap<String, Int>   // <4>
    // end::inject[]
}
