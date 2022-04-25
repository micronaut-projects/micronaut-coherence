/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.coherence.annotation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Unit test for {@link AbstractNamedLiteral} equality and hash code implementations.
 */
class NamedLiteralTest {

    @ParameterizedTest
    @ValueSource(classes = {CacheName.Literal.class,
                            MapName.Literal.class,
                            ScopeName.Literal.class,
                            SerializerFormat.Literal.class,
                            SessionName.Literal.class,
                            SubscriberGroup.Literal.class})
    void testEquals(Class<? extends AbstractNamedLiteral<?>> clz) throws Exception {
        Annotation instance1 = findAnnotation(clz, "obj1");
        Annotation instance2 = findAnnotation(clz, "obj2");
        Annotation instance3 = findAnnotation(clz, "obj3");

        assertThat(instance1, notNullValue());
        assertThat(instance2, notNullValue());
        assertThat(instance3, notNullValue());

        assertThat(instance1, is(instance1));
        assertThat(instance1, is(instance2));
        assertThat(instance2, is(instance1));
        assertThat(instance1, not(instance3));
        assertThat(instance2, not(instance3));
        assertThat(instance3, not(instance1));
        assertThat(instance3, not(instance2));
    }

    @ParameterizedTest
    @ValueSource(classes = {CacheName.Literal.class,
            MapName.Literal.class,
            ScopeName.Literal.class,
            SerializerFormat.Literal.class,
            SessionName.Literal.class,
            SubscriberGroup.Literal.class})
    void testHashCode(Class<? extends AbstractNamedLiteral<?>> clz) throws Exception {
        Annotation instance1 = findAnnotation(clz, "obj1");
        Annotation instance2 = findAnnotation(clz, "obj2");
        Annotation instance3 = findAnnotation(clz, "obj3");

        assertThat(instance1, notNullValue());
        assertThat(instance2, notNullValue());
        assertThat(instance3, notNullValue());

        assertThat(instance1.hashCode(), is(instance1.hashCode()));
        assertThat(instance1.hashCode(), is(instance2.hashCode()));
        assertThat(instance2.hashCode(), is(instance1.hashCode()));
        assertThat(instance1.hashCode(), not(instance3.hashCode()));
        assertThat(instance2.hashCode(), not(instance3.hashCode()));
        assertThat(instance3.hashCode(), not(instance1.hashCode()));
        assertThat(instance3.hashCode(), not(instance2.hashCode()));
    }

    private Annotation findAnnotation(Class<? extends Annotation> clz, String fieldName) throws Exception {
        Field classMemberField = NamedLiteralTest.class.getDeclaredField(fieldName);
        Annotation[] annotations = classMemberField.getDeclaredAnnotations();
        String name = clz.getName();
        return Arrays.stream(annotations).filter(annotation -> annotation.annotationType()
                .getName().equals(name.substring(0, name.indexOf("$")))).findFirst().orElse(null);
    }

    @SuppressWarnings("unused")
    @CacheName("test")
    @MapName("test")
    @ScopeName("test")
    @SerializerFormat("test")
    @SessionName("test")
    @SubscriberGroup("test")
    Object obj1 = new Object();

    @SuppressWarnings("unused")
    @CacheName("test")
    @MapName("test")
    @ScopeName("test")
    @SerializerFormat("test")
    @SessionName("test")
    @SubscriberGroup("test")
    Object obj2 = new Object();

    @SuppressWarnings("unused")
    @CacheName("test2")
    @MapName("test2")
    @ScopeName("test2")
    @SerializerFormat("test2")
    @SessionName("test2")
    @SubscriberGroup("test2")
    Object obj3 = new Object();
}
