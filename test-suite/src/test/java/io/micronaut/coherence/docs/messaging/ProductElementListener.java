package io.micronaut.coherence.docs.messaging;

// tag::imports[]
import com.tangosol.net.topic.Subscriber.Element;
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.coherence.docs.model.Product;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MessagingTest")
@Singleton
public class ProductElementListener {

    final List<Element<Product>> elements = new CopyOnWriteArrayList<>();

    // tag::element[]
    @CoherenceTopicListener
    @Topic("products")
    public void receive(Element<Product> product) {
        elements.add(product);  // ... process message ...
    }
    // end::element[]
}
