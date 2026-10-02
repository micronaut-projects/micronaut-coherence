package io.micronaut.coherence.docs.events.coherenceevents;

// tag::imports[]
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Executed;
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
public class EntryProcessorListener {

    final List<String> events = new CopyOnWriteArrayList<>();

    // tag::all[]
    @CoherenceEventListener
    public void onEvent(EntryProcessorEvent event) {
        record("onEvent", event);  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    public void onExecuted(@Executed EntryProcessorEvent event) {
        record("onExecuted", event);  // process the event
    }
    // end::types[]

    // tag::mapName[]
    @CoherenceEventListener
    public void onOrdersEvent(@MapName("orders") EntryProcessorEvent event) {
        record("onOrdersEvent", event);  // process the event
    }
    // end::mapName[]

    // tag::serviceName[]
    @CoherenceEventListener
    public void onStorageServiceEvent(@ServiceName("StorageService") EntryProcessorEvent event) {
        record("onStorageServiceEvent", event);  // process the event
    }
    // end::serviceName[]

    // tag::sessionName[]
    @CoherenceEventListener
    public void onBackEndEvent(@ScopeName("BackEnd") EntryProcessorEvent event) {
        record("onBackEndEvent", event);  // process the event
    }
    // end::sessionName[]

    private void record(String listener, EntryProcessorEvent event) {
        events.add(listener + ":" + event.getType() + ":" + event.getCacheName());
    }
}
