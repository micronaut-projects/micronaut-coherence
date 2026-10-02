package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.application.LifecycleEvent
import io.micronaut.coherence.annotation.Activated
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Disposing
import jakarta.inject.Singleton

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class LifecycleListener {

    final List<String> events = new CopyOnWriteArrayList<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(LifecycleEvent event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    void onActivatedOrDisposing(@Activated @Disposing LifecycleEvent event) {
        record("onActivatedOrDisposing", event)  // process the event
    }
    // end::types[]

    private void record(String listener, LifecycleEvent event) {
        events.add("$listener:${event.type}".toString())
    }
}
