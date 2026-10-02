# tag::imports[]
from typing import Annotated

from com.tangosol.net.topic import NamedTopic
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, SessionName
from micronaut.coherence.examples.model import Order, Person
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="TopicInjectionTest")
@Singleton
class TopicsBean:

    # tag::inject[]
    people: Annotated[NamedTopic[Person], Inject]
    # end::inject[]

    # tag::name[]
    orders: Annotated[NamedTopic[Order], Inject, Name("orders")]
    # end::name[]

    # tag::session[]
    topic: Annotated[NamedTopic[Order], Inject, SessionName("Customers"), Name("orders")]
    # end::session[]
