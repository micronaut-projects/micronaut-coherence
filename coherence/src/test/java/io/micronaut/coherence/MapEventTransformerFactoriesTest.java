/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.coherence;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.MapEventTransformerBinding;
import com.oracle.coherence.inject.MapEventTransformerFactory;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false)
class MapEventTransformerFactoriesTest {

    @Inject
    @TestTransformer
    MapEventTransformer<String, String, String> transformerOne;

    @Inject
    @TestTransformer("bar")
    MapEventTransformer<String, String, String> transformerTwo;

    @Test
    void shouldInjectCustomTransformer() {
        assertThat(transformerOne, is(instanceOf(CustomTransformer.class)));
        assertThat(((CustomTransformer) transformerOne).getSuffix(), is("foo"));
        assertThat(transformerTwo, is(instanceOf(CustomTransformer.class)));
        assertThat(((CustomTransformer) transformerTwo).getSuffix(), is("bar"));
    }

    @Inherited
    @MapEventTransformerBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestTransformer {
        String value() default "foo";
    }

    @Singleton
    @TestTransformer
    public static class TestTransformerFactory implements MapEventTransformerFactory<TestTransformer, String, String, String> {
        @Override
        public MapEventTransformer<String, String, String> create(TestTransformer annotation) {
            return new CustomTransformer(annotation.value());
        }
    }

    /**
     * A custom implementation of a {@link MapEventTransformer}.
     */
    static class CustomTransformer implements MapEventTransformer<String, String, String> {

        private final String suffix;

        CustomTransformer(String suffix) {
            this.suffix = suffix;
            }

        @Override
        @SuppressWarnings("unchecked")
        public MapEvent<String, String> transform(MapEvent<String, String> event) {
            String sOld = transform(event.getOldValue());
            String sNew = transform(event.getNewValue());
            return new MapEvent<String, String>(event.getMap(), event.getId(), event.getKey(), sOld, sNew);
        }

        public String transform(String value) {
            return value == null ? null : value + "-" + suffix;
        }

        public String getSuffix() {
            return suffix;
        }
    }
}
