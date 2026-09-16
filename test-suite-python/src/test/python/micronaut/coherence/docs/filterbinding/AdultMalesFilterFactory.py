# tag::imports[]
from com.tangosol.util import Extractors, Filter, Filters
from jakarta.inject import Singleton
from micronaut.coherence import FilterFactory
from micronaut.coherence.examples.model import Person

from .AdultMales import AdultMales
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", pattern="NamedMapInjectionTest|FilterBindingTest")
# tag::clazz[]
@AdultMales    # <1>
@Singleton     # <2>
class AdultMalesFilterFactory(FilterFactory["AdultMales", Person]):

    def create(self, annotation: AdultMales) -> Filter[Person]:       # <3>
        male = Filters.equal("gender", "male")
        adult = Filters.greaterEqual(Extractors.extract("age"), 18)
        return Filters.all(male, adult)
# end::clazz[]
