package io.micronaut.coherence.docs.messaging

// tag::imports[]
import io.micronaut.coherence.annotation.CoherenceTopicListener
import io.micronaut.coherence.annotation.CommitStrategy
import io.micronaut.coherence.annotation.Topic
import io.micronaut.coherence.docs.model.Product
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MessagingTest")
@Singleton
class AsyncCommitListener {

    val products = CopyOnWriteArrayList<Product>()

    // tag::async[]
    @CoherenceTopicListener(commitStrategy = CommitStrategy.ASYNC)
    @Topic("products")
    fun receive(product: Product) {
        products.add(product)  // ... process message ...
    }
    // end::async[]
}
