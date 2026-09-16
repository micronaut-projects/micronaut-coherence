# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import Publisher
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Order
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
# tag::clazz[]
@Controller
class OrderController:

    def __init__(self, topic: Annotated[Publisher[Order], Name("orders")]):
        self.topic = topic
# end::clazz[]
