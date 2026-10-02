package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.Coherence
import com.tangosol.net.events.SessionLifecycleEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.Started
import io.micronaut.coherence.annotation.Stopped
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class SessionLifecycleListener {

    val events = CopyOnWriteArrayList<String>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: SessionLifecycleEvent) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    fun onStartedOrStopped(@Started @Stopped event: SessionLifecycleEvent) {
        record("onStartedOrStopped", event)  // process the event
    }
    // end::types[]

    // tag::name[]
    @CoherenceEventListener
    fun onCustomersEvent(@Name("Customers") event: SessionLifecycleEvent) {
        record("onCustomersEvent", event)  // process the event
    }
    // end::name[]

    // tag::defaultName[]
    @CoherenceEventListener
    fun onDefaultEvent(@Name(Coherence.DEFAULT_NAME) event: SessionLifecycleEvent) {
        record("onDefaultEvent", event)  // process the event
    }
    // end::defaultName[]

    private fun record(listener: String, event: SessionLifecycleEvent) {
        events.add("$listener:${event.type}:${event.session.name}")
    }
}
