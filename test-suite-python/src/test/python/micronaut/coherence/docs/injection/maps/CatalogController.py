# tag::imports[]
from typing import Annotated

from com.tangosol.net import NamedMap
from micronaut.coherence.annotation import Name, SessionName
from micronaut.coherence.examples.model import Product
from micronaut.http.annotation import Controller
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="NamedMapInjectionTest")
# tag::clazz[]
@Controller
class CatalogController:

    def __init__(self, products: Annotated[NamedMap[str, Product], SessionName("Catalog"), Name("products")]):
        self.products = products
# end::clazz[]
