package io.micronaut.coherence.docs.messaging;

// tag::imports[]
import com.tangosol.net.topic.Subscriber.Element;
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.CommitStrategy;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.coherence.docs.model.Product;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MessagingTest")
@Singleton
public class ManualCommitListener {

    final List<Product> products = new CopyOnWriteArrayList<>();

    // tag::manual[]
    @CoherenceTopicListener(commitStrategy = CommitStrategy.MANUAL)
    @Topic("products")
    public void receive(Element<Product> element) {
        products.add(element.getValue());  // ... process message ...

        // manually commit the element
        element.commit();
    }
    // end::manual[]
}
