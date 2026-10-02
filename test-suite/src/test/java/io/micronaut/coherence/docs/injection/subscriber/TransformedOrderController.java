package io.micronaut.coherence.docs.injection.subscriber;

// tag::imports[]
import com.tangosol.net.topic.Subscriber;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.PropertyExtractor;
import io.micronaut.coherence.docs.model.Order;
import io.micronaut.http.annotation.Controller;
import jakarta.inject.Inject;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "TopicInjectionTest")
// tag::clazz[]
@Controller
public class TransformedOrderController {

    private final Subscriber<String> topic;

    @Inject
    public TransformedOrderController(@Name("orders") @PropertyExtractor("productId")
                                  Subscriber<String> topic) {
        this.topic = topic;
    }

    public Subscriber<String> getTopic() {
        return topic;
    }
}
// end::clazz[]
