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

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class EntryListener {

    final List<String> events = new CopyOnWriteArrayList<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(EntryEvent<?, ?> event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    void onInsertedOrRemoved(@Inserted @Removed EntryEvent<?, ?> event) {
        record("onInsertedOrRemoved", event)  // process the event
    }
    // end::types[]

    // tag::mapName[]
    @CoherenceEventListener
    void onOrdersEvent(@MapName("orders") EntryEvent<?, ?> event) {
        record("onOrdersEvent", event)  // process the event
    }
    // end::mapName[]

    // tag::serviceName[]
    @CoherenceEventListener
    void onStorageServiceEvent(@ServiceName("StorageService") EntryEvent<?, ?> event) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    // tag::sessionName[]
    @CoherenceEventListener
    void onBackEndEvent(@ScopeName("BackEnd") EntryEvent<?, ?> event) {
        record("onBackEndEvent", event)  // process the event
    }
    // end::sessionName[]

    private void record(String listener, EntryEvent<?, ?> event) {
        events.add("$listener:${event.type}:${event.key}".toString())
    }
}
