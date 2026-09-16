package io.micronaut.coherence.docs.transientinjection

/**
 * A service that converts values.
 */
interface Converter<F, T> {
    fun convert(value: F): T
}
