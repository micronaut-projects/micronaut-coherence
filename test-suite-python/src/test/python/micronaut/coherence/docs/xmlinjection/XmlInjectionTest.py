from time import sleep
from typing import Annotated

from com.tangosol.net import Session
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .MyInterceptor import MyInterceptor


@Property(name="coherence.sessions.interceptors.config", value="interceptor-cache-config.xml")
@Property(name="coherence.sessions.interceptors.scope-name", value="Interceptors")
@MicronautTest
class XmlInjectionTest:
    session: Annotated[Session, Inject, Name("interceptors")]
    interceptor: Annotated[MyInterceptor, Inject]

    @Test
    def test_injected_interceptor(self):
        map = self.session.getMap("foo")
        map.put("a", "1")
        map.put("a", "2")
        map.remove("a")
        for _ in range(600):
            if len(self.interceptor.events) >= 3:
                break
            sleep(0.05)
        assert self.interceptor.events == ["INSERTED:a", "UPDATED:a", "REMOVED:a"], self.interceptor.events
