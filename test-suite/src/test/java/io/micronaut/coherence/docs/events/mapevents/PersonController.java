package io.micronaut.coherence.docs.events.mapevents;

// tag::imports[]
import com.tangosol.util.MapEvent;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Inserted;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.http.annotation.Controller;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MapEventsTest")
// tag::clazz[]
@Controller                                                    // <1>
public class PersonController {

    final List<Person> newPeople = Collections.synchronizedList(new ArrayList<>());

    @CoherenceEventListener                                    // <2>
    public void onNewPerson(@MapName("people")                 // <3>
                            @Inserted                          // <4>
                            MapEvent<String, Person> event) {
        newPeople.add(event.getNewValue());                    // process the event
    }
}
// end::clazz[]
