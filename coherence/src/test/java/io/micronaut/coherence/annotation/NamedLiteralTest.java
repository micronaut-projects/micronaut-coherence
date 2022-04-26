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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Unit test for {@link AbstractNamedLiteral} equality and hash code implementations.
 */
class NamedLiteralTest {

    @Test
    void testCacheNameEquality() {
        validateEquality(CacheName.Literal.of("test"), CacheName.Literal.of("test"), CacheName.Literal.of("test2"));
    }

    @Test
    void testCacheNameHashCode() {
        validateHashCode(CacheName.Literal.of("test"), CacheName.Literal.of("test"), CacheName.Literal.of("test2"));
    }

    @Test
    void testMapNameEquality() {
        validateEquality(MapName.Literal.of("test"), MapName.Literal.of("test"), MapName.Literal.of("test2"));
    }

    @Test
    void testMapNameHashCode() {
        validateHashCode(MapName.Literal.of("test"), MapName.Literal.of("test"), MapName.Literal.of("test2"));
    }

    @Test
    void testScopeNameEquality() {
        validateEquality(ScopeName.Literal.of("test"), ScopeName.Literal.of("test"), ScopeName.Literal.of("test2"));
    }

    @Test
    void testScopeNameHashCode() {
        validateHashCode(ScopeName.Literal.of("test"), ScopeName.Literal.of("test"), ScopeName.Literal.of("test2"));
    }

    @Test
    void testSerializerFormatEquality() {
        validateEquality(SerializerFormat.Literal.of("test"), SerializerFormat.Literal.of("test"),
                SerializerFormat.Literal.of("test2"));
    }

    @Test
    void testSerializerFormatHashCode() {
        validateHashCode(SerializerFormat.Literal.of("test"), SerializerFormat.Literal.of("test"),
                SerializerFormat.Literal.of("test2"));
    }

    @Test
    void testSessionNameEquality() {
        validateEquality(SessionName.Literal.of("test"), SessionName.Literal.of("test"),
                SessionName.Literal.of("test2"));
    }

    @Test
    void testSessionNameHashCode() {
        validateHashCode(SessionName.Literal.of("test"), SessionName.Literal.of("test"),
                SessionName.Literal.of("test2"));
    }

    @Test
    void testSubscriberGroupEquality() {
        validateEquality(SubscriberGroup.Literal.of("test"), SubscriberGroup.Literal.of("test"),
                SubscriberGroup.Literal.of("test2"));
    }

    @Test
    void testSubscriberGroupHashCode() {
        validateHashCode(SubscriberGroup.Literal.of("test"), SubscriberGroup.Literal.of("test"),
                SubscriberGroup.Literal.of("test2"));
    }

    private void validateEquality(AbstractNamedLiteral<?> one, AbstractNamedLiteral<?> two,
                                  AbstractNamedLiteral<?> three) {
        assertThat(one, is(one));
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, not(three));
        assertThat(two, not(three));
        assertThat(three, not(one));
        assertThat(three, not(two));
    }

    private void validateHashCode(AbstractNamedLiteral<?> one, AbstractNamedLiteral<?> two,
                                  AbstractNamedLiteral<?> three) {
        assertThat(one.hashCode(), is(one.hashCode()));
        assertThat(one.hashCode(), is(two.hashCode()));
        assertThat(two.hashCode(), is(one.hashCode()));
        assertThat(one.hashCode(), not(three.hashCode()));
        assertThat(two.hashCode(), not(three.hashCode()));
        assertThat(three.hashCode(), not(one.hashCode()));
        assertThat(three.hashCode(), not(two.hashCode()));
    }
}
