package io.micronaut.coherence.docs.xmlinjection

import com.tangosol.net.NamedMap
import com.tangosol.net.Session
import io.micronaut.coherence.annotation.Name
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Property(name = "coherence.sessions.interceptors.config", value = "interceptor-cache-config.xml")
@Property(name = "coherence.sessions.interceptors.scope-name", value = "Interceptors")
@MicronautTest
class XmlInjectionSpec extends Specification {

    @Inject
    @Name("interceptors")
    Session session

    @Inject
    MyInterceptor interceptor

    void "test injected interceptor"() {
        given:
        NamedMap<String, String> map = session.getMap("foo")

        when:
        map.put("a", "1")
        map.put("a", "2")
        map.remove("a")

        then:
        new PollingConditions(timeout: 30).eventually {
            assert interceptor.events == ["INSERTED:a", "UPDATED:a", "REMOVED:a"]
        }
    }
}
