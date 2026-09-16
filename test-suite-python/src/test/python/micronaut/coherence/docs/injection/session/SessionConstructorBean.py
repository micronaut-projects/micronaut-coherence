# tag::imports[]
from com.tangosol.net import Session
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="SessionInjectionTest")
# tag::clazz[]
@Controller
class SessionConstructorBean:

    def __init__(self, session: Session):
        self.session = session
# end::clazz[]
