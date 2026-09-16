# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import NamedTopic
from micronaut.coherence.annotation import Name, SessionName
from micronaut.coherence.examples.model import Order
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
# tag::clazz[]
@Controller
class OrderProcessor:

    def __init__(self, orders: Annotated[NamedTopic[Order], SessionName("Customers"), Name("orders")]):
        self.orders = orders
# end::clazz[]
