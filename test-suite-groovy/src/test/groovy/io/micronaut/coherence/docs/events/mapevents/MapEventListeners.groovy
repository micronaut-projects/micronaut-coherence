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

import java.util.List
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class MapEventListeners {

    final Map<String, List<MapEvent<?, ?>>> events = new ConcurrentHashMap<>()

    // tag::all[]
    @CoherenceEventListener
    void onEvent(MapEvent<String, String> event) {
        record("onEvent", event)  // process the event
    }
    // end::all[]

    // tag::mapName[]
    @CoherenceEventListener
    void onFooMapEvent(@MapName("foo")  // <1>
                       MapEvent<String, String> event) {
        record("onFooMapEvent", event)  // process the event
    }
    // end::mapName[]

    // tag::cacheName[]
    @CoherenceEventListener
    void onFooCacheEvent(@CacheName("foo")  // <1>
                         MapEvent<String, String> event) {
        record("onFooCacheEvent", event)  // process the event
    }
    // end::cacheName[]

    // tag::serviceName[]
    @CoherenceEventListener
    void onStorageFooEvent(@MapName("foo")
                           @ServiceName("StorageService")  // <1>
                           MapEvent<String, String> event) {
        record("onStorageFooEvent", event)  // process the event
    }
    // end::serviceName[]

    // tag::serviceOnly[]
    @CoherenceEventListener
    void onStorageEvent(@ServiceName("StorageService")  // <1>
                        MapEvent<String, String> event) {
        record("onStorageEvent", event)  // process the event
    }
    // end::serviceOnly[]

    // tag::allSessions[]
    @CoherenceEventListener
    void onOrdersEvent(@MapName("orders")  // <1>
                       MapEvent<String, Order> event) {
        record("onOrdersEvent", event)  // process the event
    }
    // end::allSessions[]

    // tag::sessionName[]
    @CoherenceEventListener
    void onCustomerOrdersEvent(@MapName("orders")
                               @SessionName("Customer")  // <1>
                               MapEvent<String, Order> event) {
        record("onCustomerOrdersEvent", event)  // process the event
    }
    // end::sessionName[]

    // tag::sessionOnly[]
    @CoherenceEventListener
    void onCustomerEvent(@SessionName("Customer")  // <1>
                         MapEvent<String, Order> event) {
        record("onCustomerEvent", event)  // process the event
    }
    // end::sessionOnly[]

    // tag::routing[]
    @CoherenceEventListener
    void onCustomerOrders(@SessionName("Customer")  // <1>
                          @MapName("orders")
                          MapEvent<String, Order> event) {
        record("onCustomerOrders", event)  // process the event
    }

    @CoherenceEventListener
    void onCatalogOrders(@SessionName("Catalog")   // <2>
                         @MapName("orders")
                         MapEvent<String, Order> event) {
        record("onCatalogOrders", event)  // process the event
    }
    // end::routing[]

    private void record(String listener, MapEvent<?, ?> event) {
        events.computeIfAbsent(listener, k -> new CopyOnWriteArrayList<>()).add(event)
    }

    List<MapEvent<?, ?>> getEvents(String listener) {
        return events.getOrDefault(listener, List.of())
    }
}
