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
    lateinit var orders: Subscriber<Order>
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    lateinit var subscriber: Subscriber<Order>
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    lateinit var customerOrders: Subscriber<Order>
    // end::session[]

    // tag::filtered[]
    @Inject
    @Name("orders")
    @WhereFilter("productId = 'AB1234'")
    lateinit var filteredOrders: Subscriber<Order>
    // end::filtered[]

    // tag::group[]
    @Inject
    @Name("orders")
    @SubscriberGroup("accounts")
    lateinit var accountOrders: Subscriber<Order>
    // end::group[]

    // tag::transformed[]
    @Inject
    @Name("orders")
    @PropertyExtractor("productId")
    lateinit var productIds: Subscriber<String>
    // end::transformed[]
}
