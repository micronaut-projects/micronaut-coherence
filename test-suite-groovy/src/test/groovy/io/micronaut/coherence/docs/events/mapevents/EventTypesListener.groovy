package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Deleted
import io.micronaut.coherence.annotation.Inserted
import io.micronaut.coherence.annotation.MapName
import jakarta.inject.Singleton

import java.util.List
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class EventTypesListener {

    final Map<String, List<MapEvent<?, ?>>> events = new ConcurrentHashMap<>()

    // tag::inserted[]
    @CoherenceEventListener
    void onInserted(@MapName("test")
                    @Inserted        // <1>
                    MapEvent<String, String> event) {
        record("onInserted", event)  // process the event
    }
    // end::inserted[]

    // tag::insertedDeleted[]
    @CoherenceEventListener
    void onInsertedOrDeleted(@MapName("test")
                             @Inserted @Deleted       // <1>
                             MapEvent<String, String> event) {
        record("onInsertedOrDeleted", event)  // process the event
    }
    // end::insertedDeleted[]

    // tag::all[]
    @CoherenceEventListener
    void onEvent(@MapName("test") MapEvent<String, String> event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    private void record(String listener, MapEvent<?, ?> event) {
        events.computeIfAbsent(listener, k -> new CopyOnWriteArrayList<>()).add(event)
    }

    List<MapEvent<?, ?>> getEvents(String listener) {
        return events.getOrDefault(listener, List.of())
    }
}
