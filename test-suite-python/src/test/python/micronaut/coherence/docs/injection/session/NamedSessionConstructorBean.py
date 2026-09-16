# tag::imports[]
from typing import Annotated

from com.tangosol.net import Session
from micronaut.coherence.annotation import Name
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="SessionInjectionTest")
# tag::clazz[]
@Controller
class NamedSessionConstructorBean:

    def __init__(self, session: Annotated[Session, Name("Catalog")]):
        self.session = session
# end::clazz[]
