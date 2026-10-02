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
    protected Publisher<Order> orders
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    protected Publisher<Order> publisher
    // end::name[]

    // tag::session[]
    @Inject
    @Name("orders")
    @SessionName("Customers")
    protected Publisher<Order> customerOrders
    // end::session[]
}
