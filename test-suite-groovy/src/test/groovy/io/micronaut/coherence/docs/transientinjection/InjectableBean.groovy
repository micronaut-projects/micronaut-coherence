package io.micronaut.coherence.docs.transientinjection

// tag::imports[]
import com.oracle.coherence.inject.Injectable
import jakarta.inject.Inject
// end::imports[]

// tag::clazz[]
class InjectableBean
        implements Injectable, Serializable {

    @Inject
    private Converter<String, String> converter

    private String text

    InjectableBean() {
    }

    InjectableBean(String text) {
        this.text = text
    }

    String getConvertedText() {
        return converter.convert(text)
    }
}
// end::clazz[]
