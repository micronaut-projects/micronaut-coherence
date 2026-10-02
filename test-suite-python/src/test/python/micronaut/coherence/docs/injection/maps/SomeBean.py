# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Singleton
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Person
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
# tag::clazz[]
@Singleton
class SomeBean:

    def __init__(self, map: Annotated[NamedMap[str, Person], Name("people")]):
        self.map = map
# end::clazz[]
