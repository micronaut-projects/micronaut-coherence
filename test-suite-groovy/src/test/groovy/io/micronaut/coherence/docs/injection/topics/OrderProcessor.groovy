package io.micronaut.coherence.docs.injection.topics

// tag::imports[]
import com.tangosol.net.topic.NamedTopic
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.docs.model.Order
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
// tag::clazz[]
@Controller
class OrderProcessor {

    final NamedTopic<Order> orders

    @Inject
    OrderProcessor(@SessionName("Customers") @Name("orders")
                   NamedTopic<Order> orders) {
        this.orders = orders
    }
}
// end::clazz[]
