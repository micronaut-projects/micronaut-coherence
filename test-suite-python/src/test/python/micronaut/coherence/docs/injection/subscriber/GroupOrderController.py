# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import Subscriber
from micronaut.coherence.annotation import Name, SubscriberGroup
from micronaut.coherence.examples.model import Order
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
# tag::clazz[]
@Controller
class GroupOrderController:

    def __init__(self, topic: Annotated[Subscriber[Order], Name("orders"), SubscriberGroup("accounts")]):
        self.topic = topic
# end::clazz[]
