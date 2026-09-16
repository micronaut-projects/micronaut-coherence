package io.micronaut.coherence.docs.messaging

// tag::imports[]
import io.micronaut.coherence.annotation.CoherencePublisher
import io.micronaut.coherence.annotation.Topic
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MessagingTest")
// tag::clazz[]
@CoherencePublisher  // <1>
interface ProductClient {

    @Topic("my-products") // <2>
    fun sendProduct(message: String) // <3>

    fun sendProduct(@Topic topic: String, message: String) // <4>
}
// end::clazz[]
