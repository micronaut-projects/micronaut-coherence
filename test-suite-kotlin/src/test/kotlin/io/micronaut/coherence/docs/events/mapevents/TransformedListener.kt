package io.micronaut.coherence.docs.events.mapevents

// tag::imports[]
import com.tangosol.util.MapEvent
import io.micronaut.coherence.annotation.CoherenceEventListener
import io.micronaut.coherence.annotation.Inserted
import io.micronaut.coherence.annotation.MapName
import io.micronaut.coherence.annotation.PropertyExtractor
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
class TransformedListener {

    val customerIds = CopyOnWriteArrayList<String>()
    val newOrders = ConcurrentHashMap<Long, String>()

    // tag::extractor[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                        // <1>
    fun onOrder(@MapName("orders")                          // <2>
                event: MapEvent<String, String>) {          // <3>
        customerIds.add(event.newValue)  // process event...
    }
    // end::extractor[]

    // tag::extractors[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                     // <1>
    @PropertyExtractor("orderId")
    fun onNewOrder(@Inserted                             // <2>
                   @MapName("orders")
                   event: MapEvent<String, List<Any>>) { // <3>
        val list = event.newValue
        val customerId = list[0] as String               // <4>
        val orderId = list[1] as Long
        newOrders[orderId] = customerId
    }
    // end::extractors[]
}
