package io.micronaut.coherence.docs.transientinjection

// tag::imports[]
import com.oracle.coherence.inject.Injectable
import jakarta.inject.Inject
import java.io.Serializable
// end::imports[]

// tag::clazz[]
class InjectableBean(private val text: String? = null) : Injectable, Serializable {

    @Inject
    private lateinit var converter: Converter<String, String>

    val convertedText: String
        get() = converter.convert(text!!)
}
// end::clazz[]
