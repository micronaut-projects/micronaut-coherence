package io.micronaut.coherence.examples.model;

import java.io.Serializable;

/**
 * An order stored in the {@code orders} map and published to the {@code orders} topic of the examples.
 */
public class Order implements Serializable {

    private long orderId;
    private String customerId;
    private String productId;

    public Order() {
    }

    public Order(long orderId, String customerId, String productId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Order{orderId=" + orderId + ", customerId='" + customerId + "', productId='" + productId + "'}";
    }
}
