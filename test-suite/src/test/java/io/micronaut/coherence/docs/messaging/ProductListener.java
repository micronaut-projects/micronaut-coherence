package io.micronaut.coherence.docs.messaging;

// tag::imports[]
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.Topic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MessagingTest")
// tag::clazz[]
@CoherenceTopicListener   // <1>
public class ProductListener {

    final List<String> products = new CopyOnWriteArrayList<>();

    @Topic("my-products")   // <2>
    public void receive(String product) { // <3>
        System.out.println("Got Product - " + product);
        products.add(product);
    }
}
// end::clazz[]
