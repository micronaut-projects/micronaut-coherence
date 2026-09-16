package io.micronaut.coherence.docs.model

/**
 * An order stored in the {@code orders} map and published to the {@code orders} topic of the examples.
 */
class Order implements Serializable {

    long orderId
    String customerId
    String productId

    Order() {
    }

    Order(long orderId, String customerId, String productId) {
        this.orderId = orderId
        this.customerId = customerId
        this.productId = productId
    }

    @Override
    String toString() {
        "Order{orderId=$orderId, customerId='$customerId', productId='$productId'}"
    }
}
