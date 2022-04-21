/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.coherence.httpsession;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import io.micronaut.context.ApplicationContext;
import io.micronaut.session.Session;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@MicronautTest(propertySources = {"classpath:micronaut-http-session-test.yaml", "classpath:pof-config.xml"})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CoherenceHttpSessionTest {
    @Inject
    ApplicationContext context;

    @Inject
    Coherence coherence;

    @Test
    void shouldCreateSession() throws Exception {
        String cacheName = context.getProperty("micronaut.session.http.coherence.cache-name", String.class).orElse("http-sessions");
        NamedCache cache = coherence.getSession().getCache(cacheName);
        CoherenceSessionStore sessionStore = context.getBean(CoherenceSessionStore.class);

        // create and save new session
        CoherenceSessionStore.CoherenceHttpSession session = sessionStore.newSession();
        session.put("username", "fred");
        session.put("foo", new Foo("Fred", 10));
        CoherenceSessionStore.CoherenceHttpSession saved = sessionStore.save(session).get();

        assertThat(cache.size(), is(1));
        assertNotNull(saved);
        assertFalse(saved.isExpired());
        assertNotNull(saved.getMaxInactiveInterval());
        assertNotNull(saved.getCreationTime());
        assertNotNull(saved.getId());
        assertEquals("fred", saved.get("username").get());
        assertThat(saved.get("foo").get(), instanceOf(Foo.class));
        assertEquals("Fred", ((Foo) saved.get("foo").get()).getName());
        assertEquals(10, ((Foo) saved.get("foo").get()).getAge());

        // locate session
        CoherenceSessionStore.CoherenceHttpSession retrieved = sessionStore.findSession(saved.getId()).get().get();

        // is session valid
        assertNotNull(retrieved);
        assertFalse(retrieved.isExpired());
        assertNotNull(retrieved.getMaxInactiveInterval());
        assertNotNull(retrieved.getCreationTime());
        assertNotNull(retrieved.getId());
        assertThat(retrieved.get("foo", Foo.class).get(), instanceOf(Foo.class));
        assertEquals("fred", retrieved.get("username", String.class).get());
        assertEquals("Fred", retrieved.get("foo", Foo.class).get().getName());
        assertEquals(10, retrieved.get("foo", Foo.class).get().getAge());

        // modify session
        retrieved.remove("username");
        retrieved.put("more", "stuff");
        Instant now = Instant.now();
        retrieved.setLastAccessedTime(now);
        retrieved.setMaxInactiveInterval(Duration.of(10, ChronoUnit.MINUTES));
        sessionStore.save(retrieved).get();

        retrieved = sessionStore.findSession(retrieved.getId()).get().get();

        // is session valid
        assertNotNull(retrieved);
        assertFalse(retrieved.isExpired());
        assertEquals(Duration.of(10, ChronoUnit.MINUTES), retrieved.getMaxInactiveInterval());
        assertEquals(retrieved.getCreationTime().getLong(ChronoField.MILLI_OF_SECOND), saved.getCreationTime().getLong(ChronoField.MILLI_OF_SECOND));
        assertTrue(retrieved.getLastAccessedTime().isAfter(now));
        assertNotNull(retrieved.getId());
        assertFalse(retrieved.contains("username"));
        assertEquals("stuff", retrieved.get("more", String.class).get());
        assertEquals("Fred", retrieved.get("foo", Foo.class).get().getName());
        assertEquals(10, retrieved.get("foo", Foo.class).get().getAge());

        // delete session
        assertTrue(sessionStore.deleteSession(saved.getId()).get());

        Optional<CoherenceSessionStore.CoherenceHttpSession> found = sessionStore.findSession(saved.getId()).get();
        assertFalse(found.isPresent());
    }

    @Test
    void shouldExpireSession() throws Exception {
        CoherenceSessionStore sessionStore = context.getBean(CoherenceSessionStore.class);

        // create and save new session
        CoherenceSessionStore.CoherenceHttpSession session = sessionStore.newSession();
        session.put("username", "fred");
        session.put("foo", new Foo("Fred", 10));
        session.setMaxInactiveInterval(Duration.ofSeconds(1));
        Session saved = sessionStore.save(session).get();

        Thread.sleep(1100);
        assertFalse(sessionStore.findSession(saved.getId()).get().isPresent());
    }

    public static class Foo implements PortableObject, Serializable {
        private String name;
        private Integer age;

        public Foo() {
        }

        public Foo(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public void readExternal(PofReader pofReader) throws IOException {
            name = pofReader.readString(0);
            age = pofReader.readInt(1);

        }

        @Override
        public void writeExternal(PofWriter pofWriter) throws IOException {
            pofWriter.writeString(0, name);
            pofWriter.writeInt(1, age);
        }
    }
}
