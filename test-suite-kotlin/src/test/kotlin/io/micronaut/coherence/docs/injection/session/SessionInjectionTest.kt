package io.micronaut.coherence.docs.injection.session

import com.tangosol.net.Coherence
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "SessionInjectionTest")
@MicronautTest
class SessionInjectionTest {

    @Inject
    lateinit var sessionBean: SessionBean

    @Inject
    lateinit var sessionConstructorBean: SessionConstructorBean

    @Inject
    lateinit var namedSessionBean: NamedSessionBean

    @Inject
    lateinit var namedSessionConstructorBean: NamedSessionConstructorBean

    @Test
    fun testInjectDefaultSession() {
        assertEquals(Coherence.DEFAULT_NAME, sessionBean.session.name)
        assertEquals(Coherence.DEFAULT_NAME, sessionConstructorBean.session.name)
        assertEquals("people", sessionBean.session.getMap<String, Any>("people").name)
    }

    @Test
    fun testInjectNamedSession() {
        assertEquals("Catalog", namedSessionBean.session.name)
        assertEquals("Catalog", namedSessionConstructorBean.session.name)
        assertEquals("Catalog", namedSessionBean.session.scopeName)
    }
}
