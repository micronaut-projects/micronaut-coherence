package io.micronaut.coherence.docs.messaging;

// tag::imports[]
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.coherence.docs.model.Product;
import io.micronaut.messaging.annotation.SendTo;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MessagingTest")
// tag::clazz[]
@CoherenceTopicListener
public class ProductQuantityListener {

    @Topic("awesome-products")      // <1>
    @SendTo("product-quantities")   // <2>
    public int receive(Product product) {
        System.out.println("Got Product - " + product.getName() + " by " + product.getBrand());
        return product.getQuantity(); // <3>
    }
}
// end::clazz[]
