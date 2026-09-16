package io.micronaut.coherence.docs.events.coherenceevents;

// tag::imports[]
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Created;
import io.micronaut.coherence.annotation.Destroyed;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.annotation.ServiceName;
import io.micronaut.coherence.annotation.ScopeName;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
public class CacheLifecycleListener {

    final List<String> events = new CopyOnWriteArrayList<>();

    // tag::all[]
    @CoherenceEventListener
    public void onEvent(CacheLifecycleEvent event) {
        record("onEvent", event);  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    public void onCreatedOrDestroyed(@Created @Destroyed CacheLifecycleEvent event) {
        record("onCreatedOrDestroyed", event);  // process the event
    }
    // end::types[]

    // tag::mapName[]
    @CoherenceEventListener
    public void onOrdersEvent(@MapName("orders") CacheLifecycleEvent event) {
        record("onOrdersEvent", event);  // process the event
    }
    // end::mapName[]

    // tag::serviceName[]
    @CoherenceEventListener
    public void onStorageServiceEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
        record("onStorageServiceEvent", event);  // process the event
    }
    // end::serviceName[]

    // tag::sessionName[]
    @CoherenceEventListener
    public void onBackEndEvent(@ScopeName("BackEnd") CacheLifecycleEvent event) {
        record("onBackEndEvent", event);  // process the event
    }
    // end::sessionName[]

    private void record(String listener, CacheLifecycleEvent event) {
        events.add(listener + ":" + event.getType() + ":" + event.getCacheName());
    }
}
