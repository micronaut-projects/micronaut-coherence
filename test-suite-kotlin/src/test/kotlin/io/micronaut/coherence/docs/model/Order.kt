package io.micronaut.coherence.docs.model

import java.io.Serializable

/**
 * An order stored in the `orders` map and published to the `orders` topic of the examples.
 */
data class Order(
    var orderId: Long = 0,
    var customerId: String? = null,
    var productId: String? = null
) : Serializable
