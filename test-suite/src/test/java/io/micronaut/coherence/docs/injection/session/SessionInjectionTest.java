package io.micronaut.coherence.docs.injection.session;

import com.tangosol.net.Coherence;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "SessionInjectionTest")
@MicronautTest
class SessionInjectionTest {

    @Inject
    SessionBean sessionBean;

    @Inject
    SessionConstructorBean sessionConstructorBean;

    @Inject
    NamedSessionBean namedSessionBean;

    @Inject
    NamedSessionConstructorBean namedSessionConstructorBean;

    @Test
    void testInjectDefaultSession() {
        assertEquals(Coherence.DEFAULT_NAME, sessionBean.getSession().getName());
        assertEquals(Coherence.DEFAULT_NAME, sessionConstructorBean.getSession().getName());
        assertEquals("people", sessionBean.getSession().getMap("people").getName());
    }

    @Test
    void testInjectNamedSession() {
        assertEquals("Catalog", namedSessionBean.getSession().getName());
        assertEquals("Catalog", namedSessionConstructorBean.getSession().getName());
        assertEquals("Catalog", namedSessionBean.getSession().getScopeName());
    }
}
