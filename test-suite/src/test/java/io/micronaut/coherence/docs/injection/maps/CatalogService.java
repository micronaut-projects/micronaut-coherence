package io.micronaut.coherence.docs.injection.maps;

// tag::imports[]
import com.tangosol.net.NamedMap;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.docs.model.Product;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
@Singleton
public class CatalogService {

    // tag::session[]
    @Inject
    @SessionName("Catalog")
    @Name("products")
    NamedMap<String, Product> map;
    // end::session[]
}
