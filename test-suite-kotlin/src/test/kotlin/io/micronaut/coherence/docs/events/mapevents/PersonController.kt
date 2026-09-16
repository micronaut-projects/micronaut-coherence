package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Inserted
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.docs.model.Person
import io.micronaut.http.annotation.Controller
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
// tag::clazz[]
@Controller                                                    // <1>
class PersonController {

    val newPeople = CopyOnWriteArrayList<Person>()

    @CoherenceEventListener                                    // <2>
    fun onNewPerson(@MapName("people")                         // <3>
                    @Inserted                                  // <4>
                    event: MapEvent<String, Person>) {
        newPeople.add(event.newValue)                          // process the event
    }
}
// end::clazz[]
