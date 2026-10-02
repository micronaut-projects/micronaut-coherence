# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Person
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
@Singleton
class PeopleService:

    # tag::inject[]
    people: Annotated[NamedMap[str, Person], Inject]
    # end::inject[]

    # tag::name[]
    map: Annotated[NamedMap[str, Person], Inject, Name("people")]
    # end::name[]
