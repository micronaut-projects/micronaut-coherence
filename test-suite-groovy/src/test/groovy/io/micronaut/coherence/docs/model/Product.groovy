package io.micronaut.coherence.docs.model

/**
 * A product published to the topics of the messaging examples.
 */
class Product implements Serializable {

    String name
    String brand
    int quantity

    Product() {
    }

    Product(String name, String brand, int quantity) {
        this.name = name
        this.brand = brand
        this.quantity = quantity
    }

    @Override
    String toString() {
        "Product{name='$name', brand='$brand', quantity=$quantity}"
    }
}
