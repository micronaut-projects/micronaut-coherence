# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, View
from micronaut.coherence.examples.model import Person

from .AdultMales import AdultMales
from .Adults import Adults
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="FilterBindingTest")
@Singleton
class AdultMalesView:

    # tag::inject[]
    adult_males: Annotated[
        NamedMap[str, Person],
        Inject,
        View,               # <1>
        AdultMales,         # <2>
        Name("people"),     # <3>
    ]
    # end::inject[]

    # tag::adults[]
    adult_females: Annotated[NamedMap[str, Person], Inject, View, Adults("female"), Name("people")]
    # end::adults[]
