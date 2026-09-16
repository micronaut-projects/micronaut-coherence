# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, PropertyExtractor, View, WhereFilter
from micronaut.coherence.examples.model import Person

from ...filterbinding.AdultMales import AdultMales
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
@Singleton
class PeopleViews:

    # tag::view[]
    map: Annotated[NamedMap[str, Person], Inject, Name("people"), View]  # <1>
    # end::view[]

    # tag::filter[]
    simpsons: Annotated[NamedMap[str, Person], Inject, Name("people"), View, WhereFilter("lastName = 'Simpson'")]
    # end::filter[]

    # tag::filters[]
    adult_male_simpsons: Annotated[
        NamedMap[str, Person], Inject, Name("people"), View, WhereFilter("lastName = 'Simpson'"), AdultMales
    ]
    # end::filters[]

    # tag::extractor[]
    ages: Annotated[
        NamedMap[str, int],          # <4>
        Inject,
        View,                        # <1>
        Name("people"),              # <2>
        PropertyExtractor("age"),    # <3>
    ]
    # end::extractor[]
