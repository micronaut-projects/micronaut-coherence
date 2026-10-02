package io.micronaut.coherence.docs.events.coherenceevents;

// tag::imports[]
import com.tangosol.net.events.partition.TransferEvent;
import io.micronaut.coherence.annotation.Arrived;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Lost;
import io.micronaut.coherence.annotation.ServiceName;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
public class TransferListener {

    final List<String> events = new CopyOnWriteArrayList<>();

    // tag::all[]
    @CoherenceEventListener
    public void onEvent(TransferEvent event) {
        record("onEvent", event);  // process the event
    }
    // end::all[]

    // tag::arrived[]
    @CoherenceEventListener
    public void onArrived(@Arrived TransferEvent event) {
        boolean restored = event.getRemoteMember() == event.getLocalMember();
        record(restored ? "onRestored" : "onArrived", event);  // process the event
    }
    // end::arrived[]

    // tag::types[]
    @CoherenceEventListener
    public void onLost(@Lost TransferEvent event) {
        record("onLost", event);  // process the event
    }
    // end::types[]

    // tag::serviceName[]
    @CoherenceEventListener
    public void onStorageServiceEvent(@ServiceName("StorageService") TransferEvent event) {
        record("onStorageServiceEvent", event);  // process the event
    }
    // end::serviceName[]

    private void record(String listener, TransferEvent event) {
        events.add(listener + ":" + event.getType());
    }
}
