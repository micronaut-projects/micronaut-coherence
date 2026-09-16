package io.micronaut.coherence.docs.injection.session

import com.tangosol.net.Coherence
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "spec.name", value = "SessionInjectionTest")
@MicronautTest
class SessionInjectionSpec extends Specification {

    @Inject
    SessionBean sessionBean

    @Inject
    SessionConstructorBean sessionConstructorBean

    @Inject
    NamedSessionBean namedSessionBean

    @Inject
    NamedSessionConstructorBean namedSessionConstructorBean

    void "test inject default session"() {
        expect:
        sessionBean.session.name == Coherence.DEFAULT_NAME
        sessionConstructorBean.session.name == Coherence.DEFAULT_NAME
        sessionBean.session.getMap("people").name == "people"
    }

    void "test inject named session"() {
        expect:
        namedSessionBean.session.name == "Catalog"
        namedSessionConstructorBean.session.name == "Catalog"
        namedSessionBean.session.scopeName == "Catalog"
    }
}
