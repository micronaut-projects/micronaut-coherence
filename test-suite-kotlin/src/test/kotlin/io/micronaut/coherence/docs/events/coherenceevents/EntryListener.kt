package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.cache.EntryEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Inserted
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.annotation.Removed
import io.micronaut.coherence.annotation.ServiceName
import io.micronaut.coherence.annotation.ScopeName
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class EntryListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: EntryEvent<*, *>) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    fun onInsertedOrRemoved(@Inserted @Removed event: EntryEvent<*, *>) {
        record("onInsertedOrRemoved", event)  // process the event
    }
    // end::types[]

    // tag::mapName[]
    @CoherenceEventListener
    fun onOrdersEvent(@MapName("orders") event: EntryEvent<*, *>) {
        record("onOrdersEvent", event)  // process the event
    }
    // end::mapName[]

    // tag::serviceName[]
    @CoherenceEventListener
    fun onStorageServiceEvent(@ServiceName("StorageService") event: EntryEvent<*, *>) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    // tag::sessionName[]
    @CoherenceEventListener
    fun onBackEndEvent(@ScopeName("BackEnd") event: EntryEvent<*, *>) {
        record("onBackEndEvent", event)  // process the event
    }
    // end::sessionName[]

    private fun record(listener: String, event: EntryEvent<*, *>) {
        events.add("$listener:${event.type}:${event.key}")
    }
}
