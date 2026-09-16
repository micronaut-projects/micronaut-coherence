# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import Subscriber
from micronaut.coherence.annotation import Name, PropertyExtractor
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
# tag::clazz[]
@Controller
class TransformedOrderController:

    def __init__(self, topic: Annotated[Subscriber[str], Name("orders"), PropertyExtractor("productId")]):
        self.topic = topic
# end::clazz[]
