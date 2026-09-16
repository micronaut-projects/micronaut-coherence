# tag::imports[]
from typing import Annotated

from com.tangosol.net import AsyncNamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Person
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
@Singleton
class AsyncPeopleService:

    # tag::inject[]
    map: Annotated[AsyncNamedMap[str, Person], Inject, Name("people")]
    # end::inject[]
