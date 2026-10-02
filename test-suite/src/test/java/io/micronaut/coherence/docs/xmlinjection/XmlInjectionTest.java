package io.micronaut.coherence.docs.xmlinjection;

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "coherence.sessions.interceptors.config", value = "interceptor-cache-config.xml")
@Property(name = "coherence.sessions.interceptors.scope-name", value = "Interceptors")
@MicronautTest
class XmlInjectionTest {

    @Inject
    @Name("interceptors")
    Session session;

    @Inject
    MyInterceptor interceptor;

    @Test
    void testInjectedInterceptor() {
        NamedMap<String, String> map = session.getMap("foo");
        map.put("a", "1");
        map.put("a", "2");
        map.remove("a");
        long end = System.currentTimeMillis() + 30_000;
        while (interceptor.events.size() < 3 && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(List.of("INSERTED:a", "UPDATED:a", "REMOVED:a"), interceptor.events);
    }
}
