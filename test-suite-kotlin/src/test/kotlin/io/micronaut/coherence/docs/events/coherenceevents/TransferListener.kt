package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.TransferEvent
import io.micronaut.coherence.annotation.Arrived
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Lost
import io.micronaut.coherence.annotation.ServiceName
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class TransferListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: TransferEvent) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::arrived[]
    @CoherenceEventListener
    fun onArrived(@Arrived event: TransferEvent) {
        val restored = event.remoteMember == event.localMember
        record(if (restored) "onRestored" else "onArrived", event)  // process the event
    }
    // end::arrived[]

    // tag::types[]
    @CoherenceEventListener
    fun onLost(@Lost event: TransferEvent) {
        record("onLost", event)  // process the event
    }
    // end::types[]

    // tag::serviceName[]
    @CoherenceEventListener
    fun onStorageServiceEvent(@ServiceName("StorageService") event: TransferEvent) {
        record("onStorageServiceEvent", event)  // process the event
    }
    // end::serviceName[]

    private fun record(listener: String, event: TransferEvent) {
        events.add("$listener:${event.type}")
    }
}
