# tag::imports[]
from micronaut.coherence.annotation import CoherenceTopicListener, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::clazz[]
@CoherenceTopicListener   # <1>
class ProductListener:

    def __init__(self):
        self.products: list[str] = []

    @Topic("my-products")   # <2>
    def receive(self, product: str) -> None:  # <3>
        print(f"Got Product - {product}")
        self.products.append(product)
# end::clazz[]
