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
// tag::clazz[]
@Singleton
public class SomeBean {

    private final NamedMap<String, Person> map;

    @Inject
    public SomeBean(@Name("people") NamedMap<String, Person> map) {
        this.map = map;
    }

    public NamedMap<String, Person> getMap() {
        return map;
    }
}
// end::clazz[]
