package io.micronaut.coherence.docs.events.coherenceevents

// tag::imports[]
import com.tangosol.net.Coherence
import com.tangosol.net.events.SessionLifecycleEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.Started
import io.micronaut.coherence.annotation.Stopped
import jakarta.inject.Singleton

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CoherenceEventsTest")
@Singleton
class SessionLifecycleListener {

    final List<String> events = new CopyOnWriteArrayList<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(SessionLifecycleEvent event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::types[]
    @CoherenceEventListener
    void onStartedOrStopped(@Started @Stopped SessionLifecycleEvent event) {
        record("onStartedOrStopped", event)  // process the event
    }
    // end::types[]

    // tag::name[]
    @CoherenceEventListener
    void onCustomersEvent(@Name("Customers") SessionLifecycleEvent event) {
        record("onCustomersEvent", event)  // process the event
    }
    // end::name[]

    // tag::defaultName[]
    @CoherenceEventListener
    void onDefaultEvent(@Name(Coherence.DEFAULT_NAME) SessionLifecycleEvent event) {
        record("onDefaultEvent", event)  // process the event
    }
    // end::defaultName[]

    private void record(String listener, SessionLifecycleEvent event) {
        events.add("$listener:${event.type}:${event.session.name}")
    }
}
