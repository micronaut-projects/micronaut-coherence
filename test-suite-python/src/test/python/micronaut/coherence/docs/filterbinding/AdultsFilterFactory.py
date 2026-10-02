# tag::imports[]
from com.tangosol.util import Extractors, Filter, Filters
from jakarta.inject import Singleton
from micronaut.coherence import FilterFactory
from micronaut.coherence.examples.model import Person

from .Adults import Adults
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="FilterBindingTest")
# tag::clazz[]
@Adults("")
@Singleton
class AdultsFilterFactory(FilterFactory["Adults", Person]):

    def create(self, annotation: Adults) -> Filter[Person]:       # <1>
        gender = Filters.equal("gender", annotation.value())
        adult = Filters.greaterEqual(Extractors.extract("age"), 18)
        return Filters.all(gender, adult)
# end::clazz[]
