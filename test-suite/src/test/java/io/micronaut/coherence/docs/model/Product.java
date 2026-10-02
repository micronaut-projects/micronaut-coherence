package io.micronaut.coherence.docs.model;

import java.io.Serializable;

/**
 * A product published to the topics of the messaging examples.
 */
public class Product implements Serializable {

    private String name;
    private String brand;
    private int quantity;

    public Product() {
    }

    public Product(String name, String brand, int quantity) {
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Product{name='" + name + "', brand='" + brand + "', quantity=" + quantity + "}";
    }
}
