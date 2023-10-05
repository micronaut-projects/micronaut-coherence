/*
 * Copyright 2017-2023 original authors
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
import com.tangosol.net.NamedCache;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.util.StringUtils;
import io.micronaut.session.InMemorySessionStore;
import io.micronaut.session.Session;
import io.micronaut.session.SessionIdGenerator;
import io.micronaut.session.SessionSettings;
import io.micronaut.session.SessionStore;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link SessionStore} that uses Coherence to store session data.
 */
@Singleton
@Primary
@Requires(property = CoherenceSessionStore.COHERENCE_SESSION_ENABLED, value = StringUtils.TRUE)
@Replaces(InMemorySessionStore.class)
public class CoherenceSessionStore implements SessionStore<CoherenceSessionStore.CoherenceHttpSession>, Serializable {

    /**
     * System property to enable {@code Coherence} as an HTTP session store.
     */
    public static final String COHERENCE_SESSION_ENABLED = SessionSettings.HTTP + ".coherence.enabled";

    /**
     * The {@link SessionIdGenerator session ID generator}.
     */
    private final SessionIdGenerator sessionIdGenerator;

    /**
     * The {@link NamedCache} storing the sessions.
     */
    private final NamedCache<String, CoherenceHttpSession> cache;

    /**
     * The {@link CoherenceHttpSessionConfiguration http session configuration}.
     */
    private final CoherenceHttpSessionConfiguration sessionConfiguration;

    /**
     * Constructs a new {@code CoherenceSessionStore}.
     *
     * @param sessionIdGenerator the {@link SessionIdGenerator session id generator}.
     * @param sessionConfiguration the {@link CoherenceHttpSessionConfiguration session configuration}
     * @param cache The {@link NamedCache} storing the sessions
     */
    public CoherenceSessionStore(SessionIdGenerator sessionIdGenerator,
                                 CoherenceHttpSessionConfiguration sessionConfiguration,
                                 @Name("${micronaut.session.http.coherence.cache-name:http-sessions}")
                                         NamedCache<String, CoherenceHttpSession> cache) {
        this.sessionIdGenerator = sessionIdGenerator;
        this.sessionConfiguration = sessionConfiguration;
        this.cache = cache;
    }

    @Override
    public CoherenceHttpSession newSession() {
        return new CoherenceHttpSession(sessionIdGenerator.generateId(), sessionConfiguration.getMaxInactiveInterval());
    }

    @Override
    public CompletableFuture<Optional<CoherenceHttpSession>> findSession(String id) {
        return findSessionInternal(id);
    }

    @Override
    public CompletableFuture<Boolean> deleteSession(String id) {
        return cache.async()
                .remove(id)
                .handleAsync((session, t) -> session != null && t == null);
    }

    @Override
    public CompletableFuture<CoherenceHttpSession> save(CoherenceHttpSession session) {
        session.setNew(false);
        session.setModified(false);
        long expireIn = session.getMaxInactiveInterval().toMillis();
        return cache.async()
                .put(session.getId(), session, expireIn)
                .thenApplyAsync(unused -> session);
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Optional<CoherenceHttpSession>> findSessionInternal(String id) {
        return cache.async()
                .invoke(id, entry -> {
                    if (!entry.isPresent()) {
                        return Optional.empty();
                    }
                    CoherenceHttpSession session = entry.getValue();
                    session.setLastAccessedTime(Instant.now());
                    entry.setValue(session);
                    entry.asBinaryEntry().expire(session.getMaxInactiveInterval().toMillis());
                    return Optional.of(session);
                }).thenApplyAsync(session -> (Optional<CoherenceHttpSession>) session);
    }

    /**
     * A representation of a http session.
     */
    public static class CoherenceHttpSession implements Session, PortableObject, Serializable {
        /**
         * The session id.
         */
        private String id;

        /**
         * The time the session was created.
         */
        private Instant creationTime;

        /**
         * The maximum time a session may be idle before being expired.
         */
        private Duration maxInactiveInterval;

        /**
         * The time the session was last accessed.
         */
        private Instant lastAccessedTime;

        /**
         * Flag indicating if the session is newly created and hasn't been saved.
         */
        private boolean isNew;

        /**
         * Flag indicating if the session has been modified.
         */
        private boolean isModified;

        /**
         * The session's attributes.
         */
        private Map<String, Object> attributes;

        /**
         * Default constructor necessary for serialization.
         */
        public CoherenceHttpSession() {
        }

        /**
         * Constructs a new {@code CoherenceHttpSession}.
         *
         * @param id the ID of this session
         * @param maxInactiveInterval the maximum time a session may be idle before being expired
         */
        public CoherenceHttpSession(String id, Duration maxInactiveInterval) {
            this.id = id;
            this.lastAccessedTime = Instant.now();
            this.creationTime = lastAccessedTime;
            this.maxInactiveInterval = maxInactiveInterval;
            this.attributes = new HashMap<>();
            this.isNew = true;
        }

        @NonNull
        @Override
        public String getId() {
            return id;
        }

        @NonNull
        @Override
        public Instant getCreationTime() {
            return creationTime;
        }

        @NonNull
        @Override
        public Instant getLastAccessedTime() {
            return lastAccessedTime;
        }

        @Override
        public Session setLastAccessedTime(Instant instant) {
            if (instant != null) {
                lastAccessedTime = instant;
            }
            return this;
        }

        @Override
        public Session setMaxInactiveInterval(Duration duration) {
            if (duration != null) {
                maxInactiveInterval = duration;
            }
            return this;
        }

        @Override
        public Duration getMaxInactiveInterval() {
            return maxInactiveInterval;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }

        /**
         * Set whether session is a newly created and unsaved.
         *
         * @param aNew is the session new or not
         */
        public void setNew(boolean aNew) {
            isNew = aNew;
        }

        @Override
        public boolean isModified() {
            return isModified;
        }

        final void setModified(boolean isModified) {
            this.isModified = isModified;
        }

        @Override
        public MutableConvertibleValues<Object> put(CharSequence key, @Nullable Object value) {
            attributes.put(key.toString(), value);
            isModified = true;
            return this;
        }

        @Override
        public MutableConvertibleValues<Object> remove(CharSequence key) {
            attributes.remove(key.toString());
            isModified = true;
            return this;
        }

        @Override
        public MutableConvertibleValues<Object> clear() {
            attributes.clear();
            isModified = true;
            return this;
        }

        @Override
        public Set<String> names() {
            return attributes.keySet();
        }

        @Override
        public Collection<Object> values() {
            return attributes.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            Object value = attributes.get(name.toString());
            if (value != null) {
                return ConversionService.SHARED.convert(value, conversionContext);
            }
            return Optional.empty();
        }

        @Override
        public void readExternal(PofReader pofReader) throws IOException {
            id = pofReader.readString(0);
            creationTime = pofReader.readObject(1);
            maxInactiveInterval = pofReader.readObject(2);
            lastAccessedTime = pofReader.readObject(3);
            isNew = pofReader.readBoolean(4);
            attributes = pofReader.readMap(5, new ConcurrentHashMap<>());
        }

        @Override
        public void writeExternal(PofWriter pofWriter) throws IOException {
            pofWriter.writeString(0, id);
            pofWriter.writeObject(1, creationTime);
            pofWriter.writeObject(2, maxInactiveInterval);
            pofWriter.writeObject(3, lastAccessedTime);
            pofWriter.writeBoolean(4, isNew);
            pofWriter.writeMap(5, attributes, String.class);
        }
    }
}
