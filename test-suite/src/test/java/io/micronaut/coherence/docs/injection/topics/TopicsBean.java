package io.micronaut.coherence.docs.injection.topics;

// tag::imports[]
import com.tangosol.net.topic.NamedTopic;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.docs.model.Order;
import io.micronaut.coherence.docs.model.Person;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "TopicInjectionTest")
@Singleton
public class TopicsBean {

    // tag::inject[]
    @Inject
    NamedTopic<Person> people;
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("orders")
    NamedTopic<Order> orders;
    // end::name[]

    // tag::session[]
    @Inject
    @SessionName("Customers")
    @Name("orders")
    NamedTopic<Order> topic;
    // end::session[]

    public NamedTopic<Person> getPeople() {
        return people;
    }

    public NamedTopic<Order> getOrders() {
        return orders;
    }

    public NamedTopic<Order> getTopic() {
        return topic;
    }
}
