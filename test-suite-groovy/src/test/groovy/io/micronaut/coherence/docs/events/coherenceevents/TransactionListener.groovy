package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.TransactionEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Committed
import io.micronaut.coherence.annotation.ServiceName
import jakarta.inject.Singleton

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class TransactionListener {

    final List<String> events = new CopyOnWriteArrayList<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(TransactionEvent event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    void onCommitted(@Committed TransactionEvent event) {
        record("onCommitted", event)  // process the event
    }
    // end::types[]

    // tag::serviceName[]
    @CoherenceEventListener
    void onStorageServiceEvent(@ServiceName("StorageService") TransactionEvent event) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    private void record(String listener, TransactionEvent event) {
        events.add("$listener:${event.type}".toString())
    }
}
