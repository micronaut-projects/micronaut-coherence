package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.TransactionEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Committed
import io.micronaut.coherence.annotation.ServiceName
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class TransactionListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: TransactionEvent) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    fun onCommitted(@Committed event: TransactionEvent) {
        record("onCommitted", event)  // process the event
    }
    // end::types[]

    // tag::serviceName[]
    @CoherenceEventListener
    fun onStorageServiceEvent(@ServiceName("StorageService") event: TransactionEvent) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    private fun record(listener: String, event: TransactionEvent) {
        events.add("$listener:${event.type}")
    }
}
