# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import Publisher
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, SessionName
from micronaut.coherence.examples.model import Order
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
@Singleton
class OrderPublishers:

    # tag::inject[]
    orders: Annotated[Publisher[Order], Inject]
    # end::inject[]

    # tag::name[]
    publisher: Annotated[Publisher[Order], Inject, Name("orders")]
    # end::name[]

    # tag::session[]
    customer_orders: Annotated[Publisher[Order], Inject, Name("orders"), SessionName("Customers")]
    # end::session[]
