# tag::imports[]
from typing import Annotated

from com.oracle.coherence.inject import Injectable
from jakarta.inject import Inject
from micronaut.core.annotation import Introspected

from .Converter import Converter
# end::imports[]


# tag::clazz[]
@Introspected
class InjectableBean(Injectable):

    converter: Annotated[Converter[str, str], Inject]
    text: str | None = None

    def __init__(self, text: str | None = None):
        self.text = text

    def get_converted_text(self) -> str:
        return self.converter.convert(self.text)
# end::clazz[]
