package io.micronaut.coherence.docs.injection.topics

// tag::imports[]
import com.tangosol.net.topic.NamedTopic
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Order
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
// tag::clazz[]
@Singleton
class SomeBean {

    final NamedTopic<Order> topic

    @Inject
    SomeBean(@Name("orders") NamedTopic<Order> topic) {
        this.topic = topic
    }
}
// end::clazz[]
