package io.micronaut.coherence.docs.transientinjection;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class TransientInjectionTest {

    @Test
    void testInjectionOnDeserialization() {
        DefaultSerializer serializer = new DefaultSerializer();
        Binary binary = ExternalizableHelper.toBinary(new InjectableBean("hello"), serializer);
        InjectableBean bean = ExternalizableHelper.fromBinary(binary, serializer);
        assertEquals("HELLO", bean.getConvertedText());
    }
}
