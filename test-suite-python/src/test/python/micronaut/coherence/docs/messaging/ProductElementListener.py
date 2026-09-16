# tag::imports[]
from com.tangosol.net.topic.Subscriber import Element
from micronaut.coherence.annotation import CoherenceTopicListener, Topic
from micronaut.coherence.examples.model import Product
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::element[]
@CoherenceTopicListener
class ProductElementListener:

    def __init__(self):
        self.elements: list = []

    @Topic("products")
    def receive(self, product: Element[Product]) -> None:
        self.elements.append(product)  # ... process message ...
    # end::element[]
