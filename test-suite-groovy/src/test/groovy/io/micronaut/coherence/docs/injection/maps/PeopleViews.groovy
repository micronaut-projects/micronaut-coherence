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
    protected NamedMap<String, Person> map
    // end::view[]

    // tag::filter[]
    @Inject
    @Name("people")
    @View
    @WhereFilter("lastName = 'Simpson'")
    protected NamedMap<String, Person> simpsons
    // end::filter[]

    // tag::filters[]
    @Inject
    @Name("people")
    @View
    @WhereFilter("lastName = 'Simpson'")
    @AdultMales
    protected NamedMap<String, Person> adultMaleSimpsons
    // end::filters[]

    // tag::extractor[]
    @Inject
    @View                                       // <1>
    @Name("people")                             // <2>
    @PropertyExtractor("age")                   // <3>
    protected NamedMap<String, Integer> ages              // <4>
    // end::extractor[]
}
