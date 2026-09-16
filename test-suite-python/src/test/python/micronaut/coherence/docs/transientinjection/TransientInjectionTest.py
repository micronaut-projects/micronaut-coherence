from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ToUpperConverter import ToUpperConverter


@MicronautTest
class TransientInjectionTest:
    converter: Annotated[ToUpperConverter, Inject]

    # TODO(python): the InjectableBean example cannot be ported: a Python class cannot be serialized by Coherence
    # (the generated Java class holds a reference to the GraalPy object), so only the converter service is tested
    @Test
    def test_converter(self):
        assert self.converter.convert("hello") == "HELLO"
