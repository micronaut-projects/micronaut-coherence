package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.partition.UnsolicitedCommitEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import jakarta.inject.Singleton

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class UnsolicitedCommitListener {

    final List<String> events = new CopyOnWriteArrayList<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(UnsolicitedCommitEvent event) {
        events.add(event.type.toString())  // process the event
    }
    // end::all[]
}
