package io.micronaut.coherence.docs.injection.subscriber;

// tag::imports[]
import com.tangosol.net.topic.Subscriber;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.PropertyExtractor;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.SubscriberGroup;
import io.micronaut.coherence.annotation.WhereFilter;
import io.micronaut.coherence.docs.model.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
public class OrderSubscribers {

    // tag::inject[]
    @Inject
    Subscriber<Order> orders;
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    Subscriber<Order> subscriber;
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    Subscriber<Order> customerOrders;
    // end::session[]

    // tag::filtered[]
    @Inject
    @Name("orders")
    @WhereFilter("productId = 'AB1234'")
    Subscriber<Order> filteredOrders;
    // end::filtered[]

    // tag::group[]
    @Inject
    @Name("orders")
    @SubscriberGroup("accounts")
    Subscriber<Order> accountOrders;
    // end::group[]

    // tag::transformed[]
    @Inject
    @Name("orders")
    @PropertyExtractor("productId")
    Subscriber<String> productIds;
    // end::transformed[]
}
