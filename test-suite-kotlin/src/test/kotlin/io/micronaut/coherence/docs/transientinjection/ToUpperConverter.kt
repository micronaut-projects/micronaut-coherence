package io.micronaut.coherence.docs.transientinjection

// tag::imports[]
import jakarta.inject.Singleton
// end::imports[]

// tag::clazz[]
@Singleton
class ToUpperConverter : Converter<String, String> {
    override fun convert(value: String): String {
        return value.uppercase()
    }
}
// end::clazz[]
