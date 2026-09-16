# tag::imports[]
from com.tangosol.util import Extractors, ValueExtractor
from jakarta.inject import Singleton
from micronaut.coherence import ExtractorFactory
from micronaut.coherence.examples.model import Person

from .PersonAge import PersonAge
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ExtractorBindingTest")
# tag::clazz[]
@PersonAge()   # <1>
@Singleton     # <2>
class PersonAgeExtractorFactory(ExtractorFactory["PersonAge", Person, int]):

    def create(self, annotation: PersonAge) -> ValueExtractor[Person, int]:       # <3>
        return Extractors.extract("age")
# end::clazz[]
