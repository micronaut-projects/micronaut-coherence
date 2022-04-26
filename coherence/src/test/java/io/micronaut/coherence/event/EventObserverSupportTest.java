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
package io.micronaut.coherence.event;

import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.SessionLifecycleEvent;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.federation.FederatedChangeEvent;
import com.tangosol.net.events.federation.FederatedConnectionEvent;
import com.tangosol.net.events.federation.FederatedPartitionEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.UnsolicitedCommitEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import com.tangosol.util.processor.ConditionalPut;
import io.micronaut.coherence.annotation.Activated;
import io.micronaut.coherence.annotation.Activating;
import io.micronaut.coherence.annotation.Arrived;
import io.micronaut.coherence.annotation.Assigned;
import io.micronaut.coherence.annotation.Backlog;
import io.micronaut.coherence.annotation.Committed;
import io.micronaut.coherence.annotation.Committing;
import io.micronaut.coherence.annotation.CommittingLocal;
import io.micronaut.coherence.annotation.CommittingRemote;
import io.micronaut.coherence.annotation.Connecting;
import io.micronaut.coherence.annotation.Created;
import io.micronaut.coherence.annotation.Departed;
import io.micronaut.coherence.annotation.Departing;
import io.micronaut.coherence.annotation.Destroyed;
import io.micronaut.coherence.annotation.Disconnected;
import io.micronaut.coherence.annotation.Disposing;
import io.micronaut.coherence.annotation.Error;
import io.micronaut.coherence.annotation.Executed;
import io.micronaut.coherence.annotation.Executing;
import io.micronaut.coherence.annotation.Inserted;
import io.micronaut.coherence.annotation.Inserting;
import io.micronaut.coherence.annotation.Lost;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.Processor;
import io.micronaut.coherence.annotation.Recovered;
import io.micronaut.coherence.annotation.Removed;
import io.micronaut.coherence.annotation.Removing;
import io.micronaut.coherence.annotation.Replicating;
import io.micronaut.coherence.annotation.Rollback;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.Started;
import io.micronaut.coherence.annotation.Starting;
import io.micronaut.coherence.annotation.Stopped;
import io.micronaut.coherence.annotation.Stopping;
import io.micronaut.coherence.annotation.Synced;
import io.micronaut.coherence.annotation.Syncing;
import io.micronaut.coherence.annotation.Truncated;
import io.micronaut.coherence.annotation.Updated;
import io.micronaut.coherence.annotation.Updating;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EventObserverSupport}.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class EventObserverSupportTest {
    @Test
    void testCreateObserverForCacheLifecycleEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Created @Destroyed @Truncated final class c { }
        Created created = c.class.getAnnotation(Created.class);
        Destroyed destroyed = c.class.getAnnotation(Destroyed.class);
        Truncated truncated = c.class.getAnnotation(Truncated.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(created);
        value.add(destroyed);
        value.add(truncated);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(CacheLifecycleEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(CacheLifecycleEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForCoherenceLifecycleEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Starting @Started @Stopping @Stopped @Name("test") final class c { }
        Starting starting = c.class.getAnnotation(Starting.class);
        Started started = c.class.getAnnotation(Started.class);
        Stopping stopping = c.class.getAnnotation(Stopping.class);
        Stopped stopped = c.class.getAnnotation(Stopped.class);
        Name name = c.class.getAnnotation(Name.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(starting);
        value.add(started);
        value.add(stopping);
        value.add(stopped);
        value.add(name);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(CoherenceLifecycleEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(CoherenceLifecycleEvent.Type.class);

        assertThat(actual, is(expected));
        assertThat(((EventObserverSupport.CoherenceLifecycleEventHandler) handler).name, is("test"));
    }

    @Test
    void testCreateObserverForCoherenceEntryEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Inserting @Inserted @Updating @Updated @Removing @Removed final class c { }
        Inserting inserting = c.class.getAnnotation(Inserting.class);
        Inserted inserted = c.class.getAnnotation(Inserted.class);
        Updating updating = c.class.getAnnotation(Updating.class);
        Updated updated = c.class.getAnnotation(Updated.class);
        Removing removing = c.class.getAnnotation(Removing.class);
        Removed removed = c.class.getAnnotation(Removed.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(inserting);
        value.add(inserted);
        value.add(updating);
        value.add(updated);
        value.add(removing);
        value.add(removed);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler = EventObserverSupport.createObserver(EntryEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(EntryEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForCoherenceEntryProcessorEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Executing @Executed @Processor(ConditionalPut.class) final class c { }
        Executing executing = c.class.getAnnotation(Executing.class);
        Executed executed = c.class.getAnnotation(Executed.class);
        Processor processor = c.class.getAnnotation(Processor.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(executing);
        value.add(executed);
        value.add(processor);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler = EventObserverSupport.createObserver(EntryProcessorEvent.class,
                observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(EntryProcessorEvent.Type.class);

        assertThat(actual, is(expected));
        assertThat(((EventObserverSupport.EntryProcessorEventHandler) handler).m_classProcessor,
                is(ConditionalPut.class));
    }

    @Test
    void testCreateObserverForLifecycleEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Activating @Activated @Disposing final class c { }
        Activating activating = c.class.getAnnotation(Activating.class);
        Activated activated = c.class.getAnnotation(Activated.class);
        Disposing disposing = c.class.getAnnotation(Disposing.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(activating);
        value.add(activated);
        value.add(disposing);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(LifecycleEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(LifecycleEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForSessionLifecycleEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Starting @Started @Stopping @Stopped @Name("test") @SessionName("test") final class c { }
        Starting starting = c.class.getAnnotation(Starting.class);
        Started started = c.class.getAnnotation(Started.class);
        Stopping stopping = c.class.getAnnotation(Stopping.class);
        Stopped stopped = c.class.getAnnotation(Stopped.class);
        Name name = c.class.getAnnotation(Name.class);
        SessionName sessionName = c.class.getAnnotation(SessionName.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(starting);
        value.add(started);
        value.add(stopping);
        value.add(stopped);
        value.add(name);
        value.add(sessionName);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(SessionLifecycleEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(SessionLifecycleEvent.Type.class);

        assertThat(actual, is(expected));
        assertThat(((EventObserverSupport.SessionLifecycleEventHandler) handler).name, is("test"));
    }

    @Test
    void testCreateObserverForTransactionEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Committing @Committed final class c { }
        Committing committing = c.class.getAnnotation(Committing.class);
        Committed committed = c.class.getAnnotation(Committed.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(committing);
        value.add(committed);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(TransactionEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(TransactionEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForTransferEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Assigned @Arrived @Departing @Departed @Lost @Recovered @Rollback final class c { }
        Assigned assigned = c.class.getAnnotation(Assigned.class);
        Arrived arrived = c.class.getAnnotation(Arrived.class);
        Departing departing = c.class.getAnnotation(Departing.class);
        Departed departed = c.class.getAnnotation(Departed.class);
        Lost lost = c.class.getAnnotation(Lost.class);
        Recovered recovered = c.class.getAnnotation(Recovered.class);
        Rollback rollback = c.class.getAnnotation(Rollback.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(assigned);
        value.add(arrived);
        value.add(departing);
        value.add(departed);
        value.add(lost);
        value.add(recovered);
        value.add(rollback);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(TransferEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(TransferEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForUnsolicitedCommitEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Committed final class c { }
        Committed committed = c.class.getAnnotation(Committed.class);

        Set<Committed> value = new java.util.HashSet<>();
        value.add(committed);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(UnsolicitedCommitEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(UnsolicitedCommitEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForFederatedChangeEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @CommittingLocal @CommittingRemote @Replicating final class c { }
        CommittingLocal committingLocal = c.class.getAnnotation(CommittingLocal.class);
        CommittingRemote committingRemote = c.class.getAnnotation(CommittingRemote.class);
        Replicating replicating = c.class.getAnnotation(Replicating.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(committingLocal);
        value.add(committingRemote);
        value.add(replicating);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(FederatedChangeEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(FederatedChangeEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForFederatedConnectionEventBacklogExcessive() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Connecting @Disconnected @Backlog(Backlog.Type.EXCESSIVE) @Error final class c { }
        Connecting connecting = c.class.getAnnotation(Connecting.class);
        Disconnected disconnected = c.class.getAnnotation(Disconnected.class);
        Backlog backlog = c.class.getAnnotation(Backlog.class);
        Error error = c.class.getAnnotation(Error.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(connecting);
        value.add(disconnected);
        value.add(backlog);
        value.add(error);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(FederatedConnectionEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.of(FederatedConnectionEvent.Type.CONNECTING,
                FederatedConnectionEvent.Type.DISCONNECTED,
                FederatedConnectionEvent.Type.BACKLOG_EXCESSIVE,
                FederatedConnectionEvent.Type.ERROR);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForFederatedConnectionEventBacklogNormal() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Connecting @Disconnected @Backlog(Backlog.Type.NORMAL) @Error final class c { }
        Connecting connecting = c.class.getAnnotation(Connecting.class);
        Disconnected disconnected = c.class.getAnnotation(Disconnected.class);
        Backlog backlog = c.class.getAnnotation(Backlog.class);
        Error error = c.class.getAnnotation(Error.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(connecting);
        value.add(disconnected);
        value.add(backlog);
        value.add(error);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(FederatedConnectionEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.of(FederatedConnectionEvent.Type.CONNECTING,
                FederatedConnectionEvent.Type.DISCONNECTED,
                FederatedConnectionEvent.Type.BACKLOG_NORMAL,
                FederatedConnectionEvent.Type.ERROR);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverForFederatedPartitionEvent() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        @Syncing @Synced final class c { }
        Syncing syncing = c.class.getAnnotation(Syncing.class);
        Synced synced = c.class.getAnnotation(Synced.class);

        Set<java.lang.annotation.Annotation> value = new java.util.HashSet<>();
        value.add(syncing);
        value.add(synced);
        when(observer.getObservedQualifiers()).thenReturn(value);

        EventObserverSupport.EventHandler handler =
                EventObserverSupport.createObserver(FederatedPartitionEvent.class, observer);

        EnumSet<?> actual = handler.eventTypes();
        EnumSet<?> expected = EnumSet.allOf(FederatedPartitionEvent.Type.class);

        assertThat(actual, is(expected));
    }

    @Test
    void testCreateObserverWithNullType() {
        ExecutableMethodEventObserver observer = mock(ExecutableMethodEventObserver.class);

        assertThrows(IllegalArgumentException.class,
                () -> EventObserverSupport.createObserver(null, observer));
    }
}
