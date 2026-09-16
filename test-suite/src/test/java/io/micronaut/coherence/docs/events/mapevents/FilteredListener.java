package io.micronaut.coherence.docs.events.mapevents;

// tag::imports[]
import com.tangosol.util.MapEvent;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.annotation.WhereFilter;
import io.micronaut.coherence.docs.model.Person;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
public class FilteredListener {

    final List<MapEvent<String, Person>> adults = new CopyOnWriteArrayList<>();

    // tag::filtered[]
    @WhereFilter("age >= 18")     // <1>
    @CoherenceEventListener
    @MapName("people")
    public void onAdult(MapEvent<String, Person> people) {
        adults.add(people);  // process event...
    }
    // end::filtered[]
}
