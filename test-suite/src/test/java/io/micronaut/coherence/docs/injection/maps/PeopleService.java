package io.micronaut.coherence.docs.injection.maps;

// tag::imports[]
import com.tangosol.net.NamedMap;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.docs.model.Person;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "NamedMapInjectionTest")
@Singleton
public class PeopleService {

    // tag::inject[]
    @Inject
    NamedMap<String, Person> people;
    // end::inject[]

    // tag::name[]
    @Inject
    @Name("people")
    NamedMap<String, Person> map;
    // end::name[]
}
