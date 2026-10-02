from com.tangosol.io import DefaultSerializer
from com.tangosol.util import ExternalizableHelper
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .InjectableBean import InjectableBean


@MicronautTest
class TransientInjectionTest:

    @Test
    def test_injection_on_deserialization(self):
        serializer = DefaultSerializer()
        binary = ExternalizableHelper.toBinary(InjectableBean("hello"), serializer)
        bean = ExternalizableHelper.fromBinary(binary, serializer)
        assert bean.get_converted_text() == "HELLO"
