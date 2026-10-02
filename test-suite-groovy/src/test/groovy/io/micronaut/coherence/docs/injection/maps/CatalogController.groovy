package io.micronaut.coherence.docs.injection.maps

// tag::imports[]
import com.tangosol.net.NamedMap
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.annotation.SessionName
import io.micronaut.coherence.docs.model.Product
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
// tag::clazz[]
@Controller
class CatalogController {

    final NamedMap<String, Product> products

    @Inject
    CatalogController(@SessionName("Catalog") @Name("products")
                      NamedMap<String, Product> products) {
        this.products = products
    }
}
// end::clazz[]
