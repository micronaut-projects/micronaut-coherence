package io.micronaut.coherence.docs.injection.publisher

// tag::imports[]
import com.tangosol.net.topic.Publisher
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Order
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
// tag::clazz[]
@Controller
class OrderController {

    final Publisher<Order> topic

    @Inject
    OrderController(@Name("orders") Publisher<Order> topic) {
        this.topic = topic
    }
}
// end::clazz[]
