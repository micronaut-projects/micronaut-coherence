package io.micronaut.coherence.docs.model

import java.io.Serializable

/**
 * A product published to the topics of the messaging examples.
 */
data class Product(
    var name: String? = null,
    var brand: String? = null,
    var quantity: Int = 0
) : Serializable
