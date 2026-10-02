package io.micronaut.coherence.docs.injection.publisher

// tag::imports[]
import com.tangosol.net.topic.Publisher
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.docs.model.Order
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
class OrderPublishers {

    // tag::inject[]
    @Inject
    lateinit var orders: Publisher<Order>
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    lateinit var publisher: Publisher<Order>
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    lateinit var customerOrders: Publisher<Order>
    // end::session[]
}
