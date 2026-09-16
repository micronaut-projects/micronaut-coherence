package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Deleted
import io.micronaut.coherence.annotation.Inserted
import io.micronaut.coherence.annotation.MapName
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class EventTypesListener {

    val events = ConcurrentHashMap<String, MutableList<MapEvent<*, *>>>()

    // tag::inserted[]
    @CoherenceEventListener
    fun onInserted(@MapName("test")
                   @Inserted        // <1>
                   event: MapEvent<String, String>) {
        record("onInserted", event)  // process the event
    }
    // end::inserted[]

    // tag::insertedDeleted[]
    @CoherenceEventListener
    fun onInsertedOrDeleted(@MapName("test")
                            @Inserted @Deleted       // <1>
                            event: MapEvent<String, String>) {
        record("onInsertedOrDeleted", event)  // process the event
    }
    // end::insertedDeleted[]

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(@MapName("test") event: MapEvent<String, String>) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    private fun record(listener: String, event: MapEvent<*, *>) {
        events.computeIfAbsent(listener) { CopyOnWriteArrayList() }.add(event)
    }

    fun getEvents(listener: String): List<MapEvent<*, *>> = events[listener] ?: emptyList()
}
