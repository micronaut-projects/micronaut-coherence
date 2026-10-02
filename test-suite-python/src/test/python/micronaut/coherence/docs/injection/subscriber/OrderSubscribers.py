# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import Subscriber
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, PropertyExtractor, SessionName, SubscriberGroup, WhereFilter
from micronaut.coherence.examples.model import Order
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
@Singleton
class OrderSubscribers:

    # tag::inject[]
    orders: Annotated[Subscriber[Order], Inject]
    # end::inject[]

    # tag::name[]
    subscriber: Annotated[Subscriber[Order], Inject, Name("orders")]
    # end::name[]

    # tag::session[]
    customer_orders: Annotated[Subscriber[Order], Inject, Name("orders"), SessionName("Customers")]
    # end::session[]

    # tag::filtered[]
    filtered_orders: Annotated[Subscriber[Order], Inject, Name("orders"), WhereFilter("productId = 'AB1234'")]
    # end::filtered[]

    # tag::group[]
    account_orders: Annotated[Subscriber[Order], Inject, Name("orders"), SubscriberGroup("accounts")]
    # end::group[]

    # tag::transformed[]
    product_ids: Annotated[Subscriber[str], Inject, Name("orders"), PropertyExtractor("productId")]
    # end::transformed[]
