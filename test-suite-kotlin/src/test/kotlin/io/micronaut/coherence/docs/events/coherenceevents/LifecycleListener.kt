package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.events.application.LifecycleEvent
import io.micronaut.coherence.annotation.Activated
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Disposing
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class LifecycleListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: LifecycleEvent) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    fun onActivatedOrDisposing(@Activated @Disposing event: LifecycleEvent) {
        record("onActivatedOrDisposing", event)  // process the event
    }
    // end::types[]

    private fun record(listener: String, event: LifecycleEvent) {
        events.add("$listener:${event.type}")
    }
}
