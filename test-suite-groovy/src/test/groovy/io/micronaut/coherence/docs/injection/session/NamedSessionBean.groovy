package io.micronaut.coherence.docs.injection.session

// tag::imports[]
import com.tangosol.net.Session
import io.micronaut.coherence.annotation.Name
import io.micronaut.http.annotation.Controller
import jakarta.inject.Inject
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "SessionInjectionTest")
// tag::clazz[]
@Controller
class NamedSessionBean {

    @Inject
    @Name("Catalog")
    protected Session session
}
// end::clazz[]
