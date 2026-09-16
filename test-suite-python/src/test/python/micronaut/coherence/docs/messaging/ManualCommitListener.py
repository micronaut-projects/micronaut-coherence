# tag::imports[]
from com.tangosol.net.topic.Subscriber import Element
from micronaut.coherence.annotation import CoherenceTopicListener, CommitStrategy, Topic
from micronaut.coherence.examples.model import Product
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::manual[]
@CoherenceTopicListener(commitStrategy=CommitStrategy.MANUAL)
class ManualCommitListener:

    def __init__(self):
        self.products: list[Product] = []

    @Topic("products")
    def receive(self, element: Element[Product]) -> None:
        self.products.append(element.getValue())  # ... process message ...

        # manually commit the element
        element.commit()
    # end::manual[]
