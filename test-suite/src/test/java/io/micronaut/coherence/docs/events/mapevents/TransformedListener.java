package io.micronaut.coherence.docs.events.mapevents;

// tag::imports[]
import com.tangosol.util.MapEvent;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Inserted;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.annotation.PropertyExtractor;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MapEventsTest")
@Singleton
public class TransformedListener {

    final List<String> customerIds = new CopyOnWriteArrayList<>();
    final Map<Long, String> newOrders = new ConcurrentHashMap<>();

    // tag::extractor[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                        // <1>
    public void onOrder(@MapName("orders")                  // <2>
                        MapEvent<String, String> event) {   // <3>
        customerIds.add(event.getNewValue());  // process event...
    }
    // end::extractor[]

    // tag::extractors[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                     // <1>
    @PropertyExtractor("orderId")
    public void onNewOrder(@Inserted                     // <2>
                           @MapName("orders")
                           MapEvent<String, List<Object>> event) {  // <3>
        List<Object> list = event.getNewValue();
        String customerId = (String) list.get(0);        // <4>
        Long orderId = (Long) list.get(1);
        newOrders.put(orderId, customerId);
    }
    // end::extractors[]
}
