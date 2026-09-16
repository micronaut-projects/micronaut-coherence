package io.micronaut.coherence.docs.model

/**
 * A person stored in the {@code people} map of the examples.
 */
class Person implements Serializable {

    String firstName
    String lastName
    int age
    String gender

    Person() {
    }

    Person(String firstName, String lastName, int age, String gender) {
        this.firstName = firstName
        this.lastName = lastName
        this.age = age
        this.gender = gender
    }

    @Override
    String toString() {
        "Person{firstName='$firstName', lastName='$lastName', age=$age, gender='$gender'}"
    }
}
