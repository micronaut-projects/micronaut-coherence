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

import java.util.Optional;

import javax.inject.Inject;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.ConfigurablePofContext;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SerializerFactoriesTest {

    @Inject
    ApplicationContext context;

    @Test
    void shouldGetJavaSerializer() {
        Optional<Serializer> optional = context.findBean(Serializer.class, Qualifiers.byName("java"));
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(instanceOf(DefaultSerializer.class)));
    }

    @Test
    void shouldGetPofSerializer() {
        Optional<Serializer> optional = context.findBean(Serializer.class, Qualifiers.byName("pof"));
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(instanceOf(ConfigurablePofContext.class)));
    }

    @Test
    void shouldNotGetJsonSerializer() {
        Optional<Serializer> optional = context.findBean(Serializer.class, Qualifiers.byName("json"));
        assertThat(optional.isPresent(), is(false));
    }
}
