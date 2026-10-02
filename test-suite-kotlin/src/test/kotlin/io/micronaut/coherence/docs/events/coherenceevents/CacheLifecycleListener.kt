package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Created
import io.micronaut.coherence.annotation.Destroyed
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.annotation.ServiceName
import io.micronaut.coherence.annotation.ScopeName
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class CacheLifecycleListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: CacheLifecycleEvent) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    fun onCreatedOrDestroyed(@Created @Destroyed event: CacheLifecycleEvent) {
        record("onCreatedOrDestroyed", event)  // process the event
    }
    // end::types[]

    // tag::mapName[]
    @CoherenceEventListener
    fun onOrdersEvent(@MapName("orders") event: CacheLifecycleEvent) {
        record("onOrdersEvent", event)  // process the event
    }
    // end::mapName[]

    // tag::serviceName[]
    @CoherenceEventListener
    fun onStorageServiceEvent(@ServiceName("StorageService") event: CacheLifecycleEvent) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    // tag::sessionName[]
    @CoherenceEventListener
    fun onBackEndEvent(@ScopeName("BackEnd") event: CacheLifecycleEvent) {
        record("onBackEndEvent", event)  // process the event
    }
    // end::sessionName[]

    private fun record(listener: String, event: CacheLifecycleEvent) {
        events.add("$listener:${event.type}:${event.cacheName}")
    }
}
