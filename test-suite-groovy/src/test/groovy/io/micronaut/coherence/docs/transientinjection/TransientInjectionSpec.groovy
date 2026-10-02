package io.micronaut.coherence.docs.transientinjection

import com.tangosol.io.DefaultSerializer
import com.tangosol.util.Binary
import com.tangosol.util.ExternalizableHelper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class TransientInjectionSpec extends Specification {

    void "test injection on deserialization"() {
        given:
        DefaultSerializer serializer = new DefaultSerializer()
        Binary binary = ExternalizableHelper.toBinary(new InjectableBean("hello"), serializer)

        when:
        InjectableBean bean = ExternalizableHelper.fromBinary(binary, serializer)

        then:
        bean.convertedText == "HELLO"
    }
}
