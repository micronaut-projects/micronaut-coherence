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

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BootstrapTest {

    @Test
    void shouldHaveStartedCoherence() throws Exception {
        Collection<Coherence> instances = Coherence.getInstances();
        assertThat(instances.size(), is(1));
        Coherence coherence = instances.iterator().next();
        assertThat(coherence.getName(), is(Coherence.DEFAULT_NAME));
        // Coherence should start in less than one minute.
        coherence.whenStarted().get(1, TimeUnit.MINUTES);
    }

    @Test
    public void shouldHaveSessions() {
        Coherence coherence = Coherence.getInstance();
        Session defaultSession = coherence.getSession(Coherence.DEFAULT_NAME);
        assertThat(defaultSession, is(notNullValue()));
        assertThat(defaultSession.getScopeName(), is(Coherence.DEFAULT_SCOPE));
        Session testSession = coherence.getSession("test");
        assertThat(testSession, is(notNullValue()));
        assertThat(testSession.getScopeName(), is("Test"));
    }
}
