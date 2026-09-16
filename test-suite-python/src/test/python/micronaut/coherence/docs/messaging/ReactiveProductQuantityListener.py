# tag::imports[]
import java
from micronaut.coherence.annotation import CoherenceTopicListener, Topic
from micronaut.coherence.examples.model import Product
from micronaut.messaging.annotation import SendTo
from org.reactivestreams import Publisher

Mono = java.type("reactor.core.publisher.Mono")
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::clazz[]
@CoherenceTopicListener
class ReactiveProductQuantityListener:

    @Topic("awesome-products")       # <1>
    @SendTo("product-quantities")    # <2>
    def receive_product(self, product_mono: Publisher[Product]) -> Publisher[int]:
        def quantity(product):
            print(f"Got Product - {product.getName()} by {product.getBrand()}")
            return product.getQuantity()  # <3>
        return Mono.from_(product_mono).map(quantity)
# end::clazz[]
