package io.micronaut.coherence.docs.injection.subscriber

// tag::imports[]
import com.tangosol.net.topic.Subscriber
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.SubscriberGroup
import io.micronaut.coherence.docs.model.Order
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
// tag::clazz[]
@Controller
class GroupOrderController @Inject constructor(
    @Name("orders") @SubscriberGroup("accounts")
    val topic: Subscriber<Order>
)
// end::clazz[]
