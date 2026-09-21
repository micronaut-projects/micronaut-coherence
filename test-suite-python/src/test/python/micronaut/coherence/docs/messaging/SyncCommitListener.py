# tag::imports[]
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceTopicListener, CommitStrategy, Topic
from micronaut.coherence.examples.model import Product
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
@Singleton
class SyncCommitListener:

    def __init__(self):
        self.products: list[Product] = []

    # tag::sync[]
    @CoherenceTopicListener(commitStrategy=CommitStrategy.SYNC)
    @Topic("products")
    def receive(self, product: Product) -> None:
        self.products.append(product)  # ... process message ...
    # end::sync[]
