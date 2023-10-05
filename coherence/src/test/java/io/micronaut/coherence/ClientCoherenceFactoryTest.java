/*
 * Copyright 2021-2023 original authors
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

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

@MicronautTest(propertySources = "classpath:client-sessions.yaml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientCoherenceFactoryTest {

    @Inject
    BeanContext context;

    @Inject
    @Name("extend")
    Session extendSession;

    @Inject
    Coherence coherence;

    @Test
    public void shouldInjectSessions() {
        assertThat(extendSession, is(notNullValue()));
    }

    @Test
    public void shouldGetSessionByName() {
        Session session = context.createBean(Session.class, "extend");
        assertThat(session, is(notNullValue()));
    }

    @Test
    public void shouldNotHaveServerSessions() {
        assertThrows(Exception.class, () -> context.createBean(Session.class));
    }

    @Test
    void shouldHaveExpectedType() {
        assertThat(coherence.getMode(), is(Coherence.Mode.Client));
    }
}
