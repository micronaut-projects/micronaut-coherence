package io.micronaut.coherence.docs.transientinjection

import com.tangosol.io.DefaultSerializer
import com.tangosol.util.ExternalizableHelper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
class TransientInjectionTest {

    @Test
    fun testInjectionOnDeserialization() {
        val serializer = DefaultSerializer()
        val binary = ExternalizableHelper.toBinary(InjectableBean("hello"), serializer)
        val bean = ExternalizableHelper.fromBinary<InjectableBean>(binary, serializer)
        assertEquals("HELLO", bean.convertedText)
    }
}
