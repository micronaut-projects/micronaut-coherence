package io.micronaut.coherence.docs.model

import java.io.Serializable

/**
 * A person stored in the `people` map of the examples.
 */
data class Person(
    var firstName: String? = null,
    var lastName: String? = null,
    var age: Int = 0,
    var gender: String? = null
) : Serializable
