package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CacheName
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.annotation.ServiceName
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.docs.model.Order
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class MapEventListeners {

    val events = ConcurrentHashMap<String, MutableList<MapEvent<*, *>>>()

    // tag::all[]
    @CoherenceEventListener
    fun onEvent(event: MapEvent<String, String>) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::mapName[]
    @CoherenceEventListener
    fun onFooMapEvent(@MapName("foo")  // <1>
                      event: MapEvent<String, String>) {
        record("onFooMapEvent", event)  // process the event
    }
    // end::mapName[]

    // tag::cacheName[]
    @CoherenceEventListener
    fun onFooCacheEvent(@CacheName("foo")  // <1>
                        event: MapEvent<String, String>) {
        record("onFooCacheEvent", event)  // process the event
    }
    // end::cacheName[]

    // tag::serviceName[]
    @CoherenceEventListener
    fun onStorageFooEvent(@MapName("foo")
                          @ServiceName("StorageService")  // <1>
                          event: MapEvent<String, String>) {
        record("onStorageFooEvent", event)  // process the event
    }
    // end::serviceName[]

    // tag::serviceOnly[]
    @CoherenceEventListener
    fun onStorageEvent(@ServiceName("StorageService")  // <1>
                       event: MapEvent<String, String>) {
        record("onStorageEvent", event)  // process the event
    }
    // end::serviceOnly[]

    // tag::allSessions[]
    @CoherenceEventListener
    fun onOrdersEvent(@MapName("orders")  // <1>
                      event: MapEvent<String, Order>) {
        record("onOrdersEvent", event)  // process the event
    }
    // end::allSessions[]

    // tag::sessionName[]
    @CoherenceEventListener
    fun onCustomerOrdersEvent(@MapName("orders")
                              @SessionName("Customer")  // <1>
                              event: MapEvent<String, Order>) {
        record("onCustomerOrdersEvent", event)  // process the event
    }
    // end::sessionName[]

    // tag::sessionOnly[]
    @CoherenceEventListener
    fun onCustomerEvent(@SessionName("Customer")  // <1>
                        event: MapEvent<String, Order>) {
        record("onCustomerEvent", event)  // process the event
    }
    // end::sessionOnly[]

    // tag::routing[]
    @CoherenceEventListener
    fun onCustomerOrders(@SessionName("Customer")  // <1>
                         @MapName("orders")
                         event: MapEvent<String, Order>) {
        record("onCustomerOrders", event)  // process the event
    }

    @CoherenceEventListener
    fun onCatalogOrders(@SessionName("Catalog")   // <2>
                        @MapName("orders")
                        event: MapEvent<String, Order>) {
        record("onCatalogOrders", event)  // process the event
    }
    // end::routing[]

    private fun record(listener: String, event: MapEvent<*, *>) {
        events.computeIfAbsent(listener) { CopyOnWriteArrayList() }.add(event)
    }

    fun getEvents(listener: String): List<MapEvent<*, *>> = events[listener] ?: emptyList()
}
