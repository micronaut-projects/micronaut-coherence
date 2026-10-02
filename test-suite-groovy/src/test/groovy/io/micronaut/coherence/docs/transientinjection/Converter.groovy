package io.micronaut.coherence.docs.transientinjection

/**
 * A service that converts values.
 *
 * @param <F> the type to convert from
 * @param <T> the type to convert to
 */
interface Converter<F, T> {
    T convert(F value)
}
