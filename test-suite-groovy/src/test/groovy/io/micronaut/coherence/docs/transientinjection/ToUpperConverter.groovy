package io.micronaut.coherence.docs.transientinjection

// tag::imports[]
import jakarta.inject.Singleton
// end::imports[]

// tag::clazz[]
@Singleton
class ToUpperConverter
        implements Converter<String, String> {
    @Override
    String convert(String s) {
        return s.toUpperCase()
    }
}
// end::clazz[]
