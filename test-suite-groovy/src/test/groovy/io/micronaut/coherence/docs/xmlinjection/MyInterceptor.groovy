package io.micronaut.coherence.docs.xmlinjection

// tag::imports[]
import com.tangosol.net.events.EventInterceptor
import com.tangosol.net.events.annotation.EntryEvents
import com.tangosol.net.events.annotation.Interceptor
import com.tangosol.net.events.partition.cache.EntryEvent
import jakarta.inject.Named
import jakarta.inject.Singleton

import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

// tag::clazz[]
@Singleton
@Named("Foo")   // <1>
@Interceptor
@EntryEvents([EntryEvent.Type.INSERTED, EntryEvent.Type.UPDATED, EntryEvent.Type.REMOVED])
class MyInterceptor implements EventInterceptor<EntryEvent<?, ?>> {

    final List<String> events = new CopyOnWriteArrayList<>()

    @Override
    void onEvent(EntryEvent<?, ?> event) {
        events.add("${event.type}:${event.key}".toString())  // process the event.
    }
}
// end::clazz[]
