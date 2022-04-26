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
package io.micronaut.coherence.events;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.SessionLifecycleEvent;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import com.tangosol.util.InvocableMap;
import data.Person;
import data.PhoneNumber;
import io.micronaut.coherence.annotation.Activated;
import io.micronaut.coherence.annotation.Activating;
import io.micronaut.coherence.annotation.CacheName;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Committed;
import io.micronaut.coherence.annotation.Committing;
import io.micronaut.coherence.annotation.Created;
import io.micronaut.coherence.annotation.Destroyed;
import io.micronaut.coherence.annotation.Disposing;
import io.micronaut.coherence.annotation.Executed;
import io.micronaut.coherence.annotation.Executing;
import io.micronaut.coherence.annotation.Inserted;
import io.micronaut.coherence.annotation.Inserting;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.Processor;
import io.micronaut.coherence.annotation.Removed;
import io.micronaut.coherence.annotation.Removing;
import io.micronaut.coherence.annotation.ScopeName;
import io.micronaut.coherence.annotation.ServiceName;
import io.micronaut.coherence.annotation.Started;
import io.micronaut.coherence.annotation.Starting;
import io.micronaut.coherence.annotation.Stopped;
import io.micronaut.coherence.annotation.Stopping;
import io.micronaut.coherence.annotation.Truncated;
import io.micronaut.coherence.annotation.Updated;
import io.micronaut.coherence.annotation.Updating;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

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

        observers.events.forEach((anEnum, integer) -> System.out.println(anEnum.getClass().getName() + "->" + anEnum +
                " count=" + integer));

        Eventually.assertDeferred(() -> observers.events, hasEntry(LifecycleEvent.Type.ACTIVATING, 6));
        Eventually.assertDeferred(() -> observers.events, hasEntry(LifecycleEvent.Type.ACTIVATED, 6));
        Eventually.assertDeferred(() -> observers.events, hasEntry(LifecycleEvent.Type.DISPOSING, 6));

        Eventually.assertDeferred(() -> observers.events, hasEntry(CacheLifecycleEvent.Type.CREATED, 3));
        Eventually.assertDeferred(() -> observers.events, hasEntry(CacheLifecycleEvent.Type.DESTROYED, 3));
        Eventually.assertDeferred(() -> observers.events, hasEntry(CacheLifecycleEvent.Type.TRUNCATED, 2));

        Eventually.assertDeferred(() -> observers.events, hasEntry(TransferEvent.Type.ASSIGNED, 257));

        Eventually.assertDeferred(() -> observers.events, hasEntry(TransactionEvent.Type.COMMITTING, 14));
        Eventually.assertDeferred(() -> observers.events, hasEntry(TransactionEvent.Type.COMMITTED, 14));

        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryProcessorEvent.Type.EXECUTING, 1));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryProcessorEvent.Type.EXECUTED, 1));

        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.INSERTING, 10));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.INSERTED, 15));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.UPDATING, 10));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.UPDATED, 15));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.REMOVING, 10));
        Eventually.assertDeferred(() -> observers.events, hasEntry(EntryEvent.Type.REMOVED, 15));

        Eventually.assertDeferred(() -> observers.events, hasEntry(CoherenceLifecycleEvent.Type.STARTING, 2));
        Eventually.assertDeferred(() -> observers.events, hasEntry(CoherenceLifecycleEvent.Type.STARTED, 2));
        Eventually.assertDeferred(() -> observers.events, hasEntry(CoherenceLifecycleEvent.Type.STOPPING, 2));
        Eventually.assertDeferred(() -> observers.events, hasEntry(CoherenceLifecycleEvent.Type.STOPPED, 2));

        Eventually.assertDeferred(() -> observers.events, hasEntry(SessionLifecycleEvent.Type.STARTING, 6));
        Eventually.assertDeferred(() -> observers.events, hasEntry(SessionLifecycleEvent.Type.STARTED, 6));
        Eventually.assertDeferred(() -> observers.events, hasEntry(SessionLifecycleEvent.Type.STOPPING, 6));
        Eventually.assertDeferred(() -> observers.events, hasEntry(SessionLifecycleEvent.Type.STOPPED, 6));
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

    @SuppressWarnings("unused")
    @Singleton
    @Requires(env = "InterceptorsTest")
    public static class TestObservers {
        final Map<Enum<?>, Integer> events = new ConcurrentHashMap<>();

        // cache lifecycle events
        @CoherenceEventListener
        void onCacheLifecycleEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        // cache lifecycle events
        @CoherenceEventListener
        void onCacheLifecycleEventStarted(@Created @ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCacheLifecycleEventDestroyed(@Destroyed @ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCacheLifecycleEventTruncated(@Truncated @ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        // Coherence lifecycle events
        @CoherenceEventListener
        void onCoherenceLifecycleEvent(CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCoherenceLifecycleEventStarting(@Starting CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCoherenceLifecycleEventStarted(@Started CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCoherenceLifecycleEventStopping(@Stopping CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        @Stopped
        void onCoherenceLifecycleEventStopped(@Stopped CoherenceLifecycleEvent event) {
            record(event);
        }

        // Session lifecycle events
        @CoherenceEventListener
        void onSessionLifecycleEvent(SessionLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onSessionLifecycleEventStarting(@Starting SessionLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onSessionLifecycleEventStarted(@Started SessionLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onSessionLifecycleEventStopping(@Stopping SessionLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onSessionLifecycleEventStopped(@Stopped SessionLifecycleEvent event) {
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
        void onEntryEventInserting(@Inserting @MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onEntryEventInserted(@Inserted @MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onEntryEventUpdating(@Updating @MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onEntryEventUpdated(@Updated @MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onEntryEventRemoving(@Removing @MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onEntryEventRemoved(@Removed @MapName("people") EntryEvent<String, Person> event) {
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
        void onLifecycleEventActivating(@Activating LifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onLifecycleEventActivated(@Activated LifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onLifecycleEventDisposed(@Disposing LifecycleEvent event) {
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

        @CoherenceEventListener
        void onTransactionEventCommitting(@Committing TransactionEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onTransactionEventCommitted(@Committed TransactionEvent event) {
            record(event);
        }

        // transfer events
        @CoherenceEventListener
        void onTransferEvent(@ScopeName("Test") @ServiceName("StorageService") TransferEvent event) {
            record(event);
        }

        synchronized void record(Event<?> event) {
            Integer counter = events.get(event.getType());
            if (counter == null) {
                counter = 0;
            }
            events.put(event.getType(), ++counter);
        }
    }
}
