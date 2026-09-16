package io.micronaut.coherence.docs.injection.maps

// tag::imports[]
import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.PropertyExtractor
import io.micronaut.coherence.annotation.View
import io.micronaut.coherence.annotation.WhereFilter
import io.micronaut.coherence.docs.filterbinding.AdultMales
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
@Singleton
class PeopleViews {

    // tag::view[]
    @Inject
    @Name("people")
    @View                                    // <1>
    lateinit var map: NamedMap<String, Person>
    // end::view[]

    // tag::filter[]
    @Inject
    @Name("people")
    @View
    @WhereFilter("lastName = 'Simpson'")
    lateinit var simpsons: NamedMap<String, Person>
    // end::filter[]

    // tag::filters[]
    @Inject
    @Name("people")
    @View
    @WhereFilter("lastName = 'Simpson'")
    @AdultMales
    lateinit var adultMaleSimpsons: NamedMap<String, Person>
    // end::filters[]

    // tag::extractor[]
    @Inject
    @View                                       // <1>
    @Name("people")                             // <2>
    @PropertyExtractor("age")                   // <3>
    lateinit var ages: NamedMap<String, Int>    // <4>
    // end::extractor[]
}
