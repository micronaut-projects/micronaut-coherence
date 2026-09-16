# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import NamedTopic
from jakarta.inject import Singleton
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Order
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
# tag::clazz[]
@Singleton
class SomeBean:

    def __init__(self, topic: Annotated[NamedTopic[Order], Name("orders")]):
        self.topic = topic
# end::clazz[]
