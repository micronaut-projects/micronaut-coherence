from typing import Annotated

from com.tangosol.net import Coherence
from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .NamedSessionBean import NamedSessionBean
from .NamedSessionConstructorBean import NamedSessionConstructorBean
from .SessionBean import SessionBean
from .SessionConstructorBean import SessionConstructorBean


@Property(name="spec.name", value="SessionInjectionTest")
@MicronautTest
class SessionInjectionTest:
    session_bean: Annotated[SessionBean, Inject]
    session_constructor_bean: Annotated[SessionConstructorBean, Inject]
    named_session_bean: Annotated[NamedSessionBean, Inject]
    named_session_constructor_bean: Annotated[NamedSessionConstructorBean, Inject]

    @Test
    def test_inject_default_session(self):
        assert self.session_bean.session.getName() == Coherence.DEFAULT_NAME
        assert self.session_constructor_bean.session.getName() == Coherence.DEFAULT_NAME
        assert self.session_bean.session.getMap("people").getName() == "people"

    @Test
    def test_inject_named_session(self):
        assert self.named_session_bean.session.getName() == "Catalog"
        assert self.named_session_constructor_bean.session.getName() == "Catalog"
        assert self.named_session_bean.session.getScopeName() == "Catalog"
