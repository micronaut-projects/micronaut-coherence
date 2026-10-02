package io.micronaut.coherence.docs.injection.publisher;

// tag::imports[]
import com.tangosol.net.topic.Publisher;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.docs.model.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
public class OrderPublishers {

    // tag::inject[]
    @Inject
    Publisher<Order> orders;
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    Publisher<Order> publisher;
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    Publisher<Order> customerOrders;
    // end::session[]

    public Publisher<Order> getOrders() {
        return orders;
    }

    public Publisher<Order> getPublisher() {
        return publisher;
    }

    public Publisher<Order> getCustomerOrders() {
        return customerOrders;
    }
}
