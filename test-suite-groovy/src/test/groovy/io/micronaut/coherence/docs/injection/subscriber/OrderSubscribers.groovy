package io.micronaut.coherence.docs.injection.subscriber

// tag::imports[]
import com.tangosol.net.topic.Subscriber
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.PropertyExtractor
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.annotation.SubscriberGroup
import io.micronaut.coherence.annotation.WhereFilter
import io.micronaut.coherence.docs.model.Order
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
class OrderSubscribers {

    // tag::inject[]
    @Inject
    protected Subscriber<Order> orders
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    protected Subscriber<Order> subscriber
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    protected Subscriber<Order> customerOrders
    // end::session[]

    // tag::filtered[]
    @Inject
    @Name("orders")
    @WhereFilter("productId = 'AB1234'")
    protected Subscriber<Order> filteredOrders
    // end::filtered[]

    // tag::group[]
    @Inject
    @Name("orders")
    @SubscriberGroup("accounts")
    protected Subscriber<Order> accountOrders
    // end::group[]

    // tag::transformed[]
    @Inject
    @Name("orders")
    @PropertyExtractor("productId")
    protected Subscriber<String> productIds
    // end::transformed[]
}
