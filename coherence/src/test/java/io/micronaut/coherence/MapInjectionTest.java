package io.micronaut.coherence;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapInjectionTest {
    @Test
    void test() {
        try (ApplicationContext ctx = ApplicationContext.run(Map.of("spec.name", "MapInjectionTest"))) {
            MapConsumer consumer = ctx.getBean(MapConsumer.class);
            assertEquals(1, consumer.map.size());
            assertEquals("foo", consumer.map.keySet().iterator().next());
        }
    }

    interface MyInterface {
    }

    @Singleton
    @Named("foo")
    @Requires(property = "spec.name", value = "MapInjectionTest")
    static final class MyNamedBean implements MyInterface {
    }

    @Singleton
    @Requires(property = "spec.name", value = "MapInjectionTest")
    static final class MapConsumer {
        final Map<String, MyInterface> map;

        MapConsumer(Map<String, MyInterface> map) {
            this.map = map;
        }
    }
}
