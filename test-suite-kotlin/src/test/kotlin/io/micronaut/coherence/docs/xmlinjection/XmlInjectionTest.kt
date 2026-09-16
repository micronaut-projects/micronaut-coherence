package io.micronaut.coherence.docs.xmlinjection

import com.tangosol.net.Session
import io.micronaut.coherence.annotation.Name
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Property(name = "coherence.sessions.interceptors.config", value = "interceptor-cache-config.xml")
@Property(name = "coherence.sessions.interceptors.scope-name", value = "Interceptors")
@MicronautTest
class XmlInjectionTest {

    @Inject
    @Name("interceptors")
    lateinit var session: Session

    @Inject
    lateinit var interceptor: MyInterceptor

    @Test
    fun testInjectedInterceptor() {
        val map = session.getMap<String, String>("foo")
        map["a"] = "1"
        map["a"] = "2"
        map.remove("a")
        val end = System.currentTimeMillis() + 30_000
        while (interceptor.events.size < 3 && System.currentTimeMillis() < end) {
            Thread.sleep(50)
        }
        assertEquals(listOf("INSERTED:a", "UPDATED:a", "REMOVED:a"), interceptor.events)
    }
}
