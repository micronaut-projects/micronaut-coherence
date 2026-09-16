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
class OrderProcessor @Inject constructor(
    @SessionName("Customers") @Name("orders")
    val orders: NamedTopic<Order>
)
// end::clazz[]
