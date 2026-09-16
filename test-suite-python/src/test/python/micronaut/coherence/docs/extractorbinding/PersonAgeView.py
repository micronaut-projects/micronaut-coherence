# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, View

from .PersonAge import PersonAge
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ExtractorBindingTest")
@Singleton
class PersonAgeView:

    # tag::inject[]
    ages: Annotated[
        NamedMap[str, int],   # <4>
        Inject,
        View,                 # <1>
        PersonAge,            # <2>
        Name("people"),       # <3>
    ]
    # end::inject[]
