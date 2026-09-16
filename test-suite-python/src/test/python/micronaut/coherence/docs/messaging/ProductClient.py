from abc import ABC, abstractmethod

# tag::imports[]
from typing import Annotated

from micronaut.coherence.annotation import CoherencePublisher, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
# tag::clazz[]
@CoherencePublisher  # <1>
class ProductClient(ABC):

    @Topic("my-products")  # <2>
    @abstractmethod
    def send_product(self, message: str) -> None:  # <3>
        ...

    @abstractmethod
    def send_product_to(self, topic: Annotated[str, Topic], message: str) -> None:  # <4>
        ...
# end::clazz[]
