# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from jakarta.inject import Inject, Singleton
from micronaut.coherence.annotation import Name, SessionName
from micronaut.coherence.examples.model import Product
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
@Singleton
class CatalogService:

    # tag::session[]
    map: Annotated[NamedMap[str, Product], Inject, SessionName("Catalog"), Name("products")]
    # end::session[]
