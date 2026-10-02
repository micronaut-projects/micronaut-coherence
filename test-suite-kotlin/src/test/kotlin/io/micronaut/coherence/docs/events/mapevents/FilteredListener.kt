package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.annotation.WhereFilter
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class FilteredListener {

    val adults = CopyOnWriteArrayList<MapEvent<String, Person>>()

    // tag::filtered[]
    @WhereFilter("age >= 18")     // <1>
    @CoherenceEventListener
    @MapName("people")
    fun onAdult(people: MapEvent<String, Person>) {
        adults.add(people)  // process event...
    }
    // end::filtered[]
}
