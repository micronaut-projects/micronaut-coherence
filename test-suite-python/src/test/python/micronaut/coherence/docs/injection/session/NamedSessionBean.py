# tag::imports[]
from typing import Annotated

from com.tangosol.net import Session
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="SessionInjectionTest")
# tag::clazz[]
@Controller
class NamedSessionBean:

    session: Annotated[Session, Inject, Name("Catalog")]
# end::clazz[]
