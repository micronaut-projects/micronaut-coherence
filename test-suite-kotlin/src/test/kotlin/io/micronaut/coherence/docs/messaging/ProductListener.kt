package io.micronaut.coherence.docs.messaging

// tag::imports[]
import io.micronaut.coherence.annotation.CoherenceTopicListener
import io.micronaut.coherence.annotation.Topic
import java.util.concurrent.CopyOnWriteArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MessagingTest")
// tag::clazz[]
@CoherenceTopicListener   // <1>
class ProductListener {

    val products = CopyOnWriteArrayList<String>()

    @Topic("my-products")   // <2>
    fun receive(product: String) { // <3>
        println("Got Product - $product")
        products.add(product)
    }
}
// end::clazz[]
