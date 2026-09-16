# tag::imports[]
from jakarta.inject import Singleton

from .Converter import Converter
# end::imports[]


# tag::clazz[]
@Singleton
class ToUpperConverter(Converter[str, str]):

    def convert(self, s: str) -> str:
        return s.upper()
# end::clazz[]
