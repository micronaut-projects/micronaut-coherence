# tag::imports[]
from micronaut.coherence.annotation import CoherenceTopicListener, Topic
from micronaut.coherence.examples.model import Product
from micronaut.messaging.annotation import SendTo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::clazz[]
@CoherenceTopicListener
class ProductQuantityListener:

    @Topic("awesome-products")      # <1>
    @SendTo("product-quantities")   # <2>
    def receive(self, product: Product) -> int:
        print(f"Got Product - {product.getName()} by {product.getBrand()}")
        return product.getQuantity()  # <3>
# end::clazz[]
