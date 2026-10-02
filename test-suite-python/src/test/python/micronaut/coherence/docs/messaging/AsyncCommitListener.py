# tag::imports[]
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceTopicListener, CommitStrategy, Topic
from micronaut.coherence.examples.model import Product
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
@Singleton
class AsyncCommitListener:

    def __init__(self):
        self.products: list[Product] = []

    # tag::async[]
    @CoherenceTopicListener(commitStrategy=CommitStrategy.ASYNC)
    @Topic("products")
    def receive(self, product: Product) -> None:
        self.products.append(product)  # ... process message ...
    # end::async[]
