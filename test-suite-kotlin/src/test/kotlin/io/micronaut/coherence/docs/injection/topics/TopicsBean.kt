package io.micronaut.coherence.docs.injection.topics

// tag::imports[]
import com.tangosol.net.topic.NamedTopic
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.docs.model.Order
import io.micronaut.coherence.docs.model.Person
import jakarta.inject.Inject
import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
class TopicsBean {

    // tag::inject[]
    @Inject
    lateinit var people: NamedTopic<Person>
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    lateinit var orders: NamedTopic<Order>
    // end::name[]

    // tag::session[]
    @Inject
    @SessionName("Customers")
    @Name("orders")
    lateinit var topic: NamedTopic<Order>
    // end::session[]
}
