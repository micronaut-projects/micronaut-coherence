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
package io.micronaut.coherence.events;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import com.tangosol.util.InvocableMap;
import data.Person;
import data.PhoneNumber;
import io.micronaut.coherence.annotation.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "InterceptorsTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterceptorsTest {

    @Inject
    ApplicationContext context;

    @Inject
    @Name("test")
    private Session session;

    @Inject
    private TestObservers observers;

    @Test
    void testEventInterceptorMethods() {
        Coherence coherence = Coherence.getInstance();
        CompletableFuture<Void> closeFuture = coherence.whenClosed();

        // Ensure that Coherence has started before stating the test
        coherence.whenStarted().join();

        NamedCache<String, Person> people = session.getCache("people");
        people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("marge", new Person("Marge", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("bart", new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("lisa", new Person("Lisa", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("maggie", new Person("Maggie", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));

        people.invokeAll(new Uppercase());

        people.clear();
        people.truncate();
        people.destroy();

        context.close();

        // ensure that Coherence is closed so that we should have the Stopped event
        closeFuture.join();

        observers.getEvents().forEach(System.err::println);

        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(LifecycleEvent.Type.ACTIVATING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(LifecycleEvent.Type.ACTIVATED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(LifecycleEvent.Type.DISPOSING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CacheLifecycleEvent.Type.CREATED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CacheLifecycleEvent.Type.DESTROYED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CacheLifecycleEvent.Type.TRUNCATED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(TransferEvent.Type.ASSIGNED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(TransactionEvent.Type.COMMITTING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(TransactionEvent.Type.COMMITTED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryProcessorEvent.Type.EXECUTING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryProcessorEvent.Type.EXECUTED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.INSERTING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.INSERTED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.UPDATING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.UPDATED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.REMOVING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(EntryEvent.Type.REMOVED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CoherenceLifecycleEvent.Type.STARTING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CoherenceLifecycleEvent.Type.STARTED));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CoherenceLifecycleEvent.Type.STOPPING));
        Eventually.assertDeferred(() -> observers.getEvents(), hasItem(CoherenceLifecycleEvent.Type.STOPPED));
    }

    /**
     * A simple entry processor to convert a {@link Person} last name to upper case.
     */
    public static class Uppercase implements InvocableMap.EntryProcessor<String, Person, Object> {
        @Override
        public Object process(InvocableMap.Entry<String, Person> entry) {
            Person p = entry.getValue();
            p.setLastName(p.getLastName().toUpperCase());
            entry.setValue(p);
            return null;
        }
    }

    @Singleton
    @Requires(env = "InterceptorsTest")
    public static class TestObservers {
        private final Map<Enum<?>, Boolean> events = new ConcurrentHashMap<>();

        Set<Enum<?>> getEvents() {
            Set<Enum<?>> set = new TreeSet<>(Comparator.comparing(Enum::name));
            set.addAll(events.keySet());
            return set;
        }

        // cache lifecycle events
        @CoherenceEventListener
        void onCacheLifecycleEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        // Coherence lifecycle events
        @CoherenceEventListener
        void onCoherenceLifecycleEvent(CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCreatedPeople(@Created @MapName("people") CacheLifecycleEvent event) {
            record(event);
            assertThat(event.getCacheName(), is("people"));
        }

        @CoherenceEventListener
        void onDestroyedPeople(@Destroyed @CacheName("people") CacheLifecycleEvent event) {
            record(event);
            assertThat(event.getCacheName(), is("people"));
        }

        // entry events
        @CoherenceEventListener
        void onEntryEvent(@MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onExecuted(@Executed @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
            record(event);
            assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
            assertThat(event.getEntrySet().size(), is(0));
        }

        @CoherenceEventListener
        void onExecuting(@Executing @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
            record(event);
            assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
            assertThat(event.getEntrySet().size(), is(5));
        }

        // lifecycle events
        @CoherenceEventListener
        void onLifecycleEvent(LifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onPersonInserted(@Inserted @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getValue().getLastName(), is("Simpson"));
        }

        @CoherenceEventListener
        void onPersonRemoved(@Removed @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getOriginalValue().getLastName(), is("SIMPSON"));
        }

        @CoherenceEventListener
        void onPersonUpdated(@Updated @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getValue().getLastName(), is("SIMPSON"));
        }

        // transaction events
        @CoherenceEventListener
        void onTransactionEvent(TransactionEvent event) {
            record(event);
        }

        // transfer events
        @CoherenceEventListener
        void onTransferEvent(@ScopeName("Test") @ServiceName("StorageService") TransferEvent event) {
            record(event);
        }

        void record(Event<?> event) {
            events.put(event.getType(), true);
        }
    }
}
