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
package io.micronaut.coherence.event;

import com.tangosol.net.CacheService;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.events.*;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.federation.FederatedChangeEvent;
import com.tangosol.net.events.federation.FederatedConnectionEvent;
import com.tangosol.net.events.federation.FederatedPartitionEvent;
import com.tangosol.net.events.internal.CoherenceEventDispatcher;
import com.tangosol.net.events.internal.ConfigurableCacheFactoryDispatcher;
import com.tangosol.net.events.internal.SessionEventDispatcher;
import com.tangosol.net.events.partition.PartitionedServiceDispatcher;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.UnsolicitedCommitEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEventDispatcher;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import io.micronaut.coherence.annotation.*;
import io.micronaut.coherence.annotation.Error;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Manages registration of observer methods with {@link InterceptorRegistry}
 * upon {@link ConfigurableCacheFactory} activation, and their subsequent un-registration on deactivation.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public final class EventObserverSupport {

    private EventObserverSupport() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E extends Event<T>, T extends Enum<T>>
    EventHandler<E, T> createObserver(Class<E> type, ExecutableMethodEventObserver<E, ?, ?> observer) {
        if (CacheLifecycleEvent.class.equals(type)) {
            return (EventHandler<E, T>) new CacheLifecycleEventHandler(((ExecutableMethodEventObserver<CacheLifecycleEvent, ?, ?>) observer));
        }
        if (CoherenceLifecycleEvent.class.equals(type)) {
            return (EventHandler<E, T>) new CoherenceLifecycleEventHandler((ExecutableMethodEventObserver<CoherenceLifecycleEvent, ?, ?>) observer);
        }
        if (EntryEvent.class.equals(type)) {
            return new EntryEventHandler(observer);
        }
        if (EntryProcessorEvent.class.equals(type)) {
            return (EventHandler<E, T>) new EntryProcessorEventHandler((ExecutableMethodEventObserver<EntryProcessorEvent, ?, ?>) observer);
        }
        if (LifecycleEvent.class.equals(type)) {
            return (EventHandler<E, T>) new LifecycleEventHandler((ExecutableMethodEventObserver<LifecycleEvent, ?, ?>) observer);
        }
        if (SessionLifecycleEvent.class.equals(type)) {
            return (EventHandler<E, T>) new SessionLifecycleEventHandler((ExecutableMethodEventObserver<SessionLifecycleEvent, ?, ?>) observer);
        }
        if (TransactionEvent.class.equals(type)) {
            return (EventHandler<E, T>) new TransactionEventHandler((ExecutableMethodEventObserver<TransactionEvent, ?, ?>) observer);
        }
        if (TransferEvent.class.equals(type)) {
            return (EventHandler<E, T>) new TransferEventHandler((ExecutableMethodEventObserver<TransferEvent, ?, ?>) observer);
        }
        if (UnsolicitedCommitEvent.class.equals(type)) {
            return (EventHandler<E, T>) new UnsolicitedCommitEventHandler((ExecutableMethodEventObserver<UnsolicitedCommitEvent, ?, ?>) observer);
        }
        if (FederatedChangeEvent.class.equals(type)) {
            return (EventHandler<E, T>) new FederatedChangeEventHandler((ExecutableMethodEventObserver<FederatedChangeEvent, ?, ?>) observer);
        }
        if (FederatedConnectionEvent.class.equals(type)) {
            return (EventHandler<E, T>) new FederatedConnectionEventHandler((ExecutableMethodEventObserver<FederatedConnectionEvent, ?, ?>) observer);
        }
        if (FederatedPartitionEvent.class.equals(type)) {
            return (EventHandler<E, T>) new FederatedPartitionEventHandler((ExecutableMethodEventObserver<FederatedPartitionEvent, ?, ?>) observer);
        }
        throw new IllegalArgumentException("Unsupported event type: " + type);
    }


    /**
     * Abstract base class for all observer-based interceptors.
     *
     * @param <E> the type of {@link Event} this interceptor accepts
     * @param <T> the enumeration of event types E supports
     */
    abstract static class EventHandler<E extends Event<T>, T extends Enum<T>>
            implements EventDispatcherAwareInterceptor<E> {

        /**
         * The observer method to delegate events to.
         */
        protected final ExecutableMethodEventObserver<E, ?, ?> observer;

        /**
         * A set of event types the observer is interested in.
         */
        protected final EnumSet<T> eventTypes;

        /**
         * The scope name for a {@link ConfigurableCacheFactory} this
         * interceptor is interested in.
         */
        private final String scopeName;

        /**
         * Construct {@code EventHandler} instance.
         *
         * @param observer       the observer method to delegate events to
         * @param classEventType the class of event type enumeration
         */
        EventHandler(ExecutableMethodEventObserver<E, ?, ?> observer, Class<T> classEventType) {
            this.observer = observer;
            this.eventTypes = EnumSet.noneOf(classEventType);

            String sScope = null;

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof ScopeName) {
                    sScope = ((ScopeName) a).value();
                }
            }

            this.scopeName = sScope;
        }

        @Override
        public void introduceEventDispatcher(String identifier, EventDispatcher dispatcher) {
            if (isApplicable(dispatcher, scopeName)) {
                dispatcher.addEventInterceptor(getId(), this, eventTypes(), false);
            }
        }

        @Override
        public void onEvent(E event) {
            if (shouldFire(event)) {
                String observerScope = scopeName;
                String eventScope = getEventScope(event);

                if (observerScope == null || eventScope == null || observerScope.equals(eventScope)) {
                    if (observer.isAsync()) {
                        CompletableFuture.supplyAsync(() -> {
                            observer.notify(event);
                            return event;
                        });
                    } else {
                        observer.notify(event);
                    }
                }
            }
        }

        /**
         * Return a unique identifier for this interceptor.
         *
         * @return a unique identifier for this interceptor
         */
        String getId() {
            return observer.getId();
        }

        /**
         * Return {@code true} if this interceptor should be registered with
         * a specified dispatcher.
         *
         * @param dispatcher a dispatcher to register this interceptor with
         * @param scopeName  a scope name the observer is interested in,
         *                   or {@code null} for all scopes
         * @return {@code true} if this interceptor should be registered with
         * a specified dispatcher; {@code false} otherwise
         */
        abstract boolean isApplicable(EventDispatcher dispatcher, String scopeName);

        /**
         * Return {@code true} if the event should fire.
         * <p>
         * This allows sub-classes to provide additional filtering logic and
         * prevent the observer method notification from happening even after
         * the Coherence server-side event is fired.
         *
         * @param event the event to check
         * @return {@code true} if the event should fire
         */
        boolean shouldFire(E event) {
            return true;
        }

        /**
         * Return the scope name of the {@link ConfigurableCacheFactory} the
         * specified event was raised from.
         *
         * @param event the event to extract scope name from
         * @return the scope name
         */
        String getEventScope(E event) {
            return null;
        }

        /**
         * Add specified event type to a set of types this interceptor should handle.
         *
         * @param type the event type to add
         */
        void addType(T type) {
            eventTypes.add(type);
        }

        /**
         * Create a final set of event types to register this interceptor for.
         *
         * @return a final set of event types to register this interceptor for
         */
        protected EnumSet<T> eventTypes() {
            return eventTypes.isEmpty() ? EnumSet.complementOf(eventTypes) : eventTypes;
        }

        /**
         * Return the name of the scope this interceptor should be registered with.
         *
         * @return the name of the scope this interceptor should be registered with
         */
        public String getScopeName() {
            return scopeName;
        }

        /**
         * Remove the scope prefix from a specified service name.
         *
         * @param serviceName the service name to remove scope prefix from
         * @return service name with scope prefix removed
         */
        protected String removeScope(String serviceName) {
            int nIndex = serviceName.indexOf(':');
            return nIndex > -1 ? serviceName.substring(nIndex + 1) : serviceName;
        }
    }

    /**
     * Handler for {@link CoherenceLifecycleEvent}s.
     */
    static class CoherenceLifecycleEventHandler
            extends EventHandler<CoherenceLifecycleEvent, CoherenceLifecycleEvent.Type> {

        private String name;

        CoherenceLifecycleEventHandler(ExecutableMethodEventObserver<CoherenceLifecycleEvent, ?, ?> observer) {
            super(observer, CoherenceLifecycleEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Starting) {
                    addType(CoherenceLifecycleEvent.Type.STARTING);
                } else if (a instanceof Started) {
                    addType(CoherenceLifecycleEvent.Type.STARTED);
                } else if (a instanceof Stopping) {
                    addType(CoherenceLifecycleEvent.Type.STOPPING);
                } else if (a instanceof Stopped) {
                    addType(CoherenceLifecycleEvent.Type.STOPPED);
                } else if (a instanceof Name) {
                    name = ((Name) a).value();
                }
            }
        }

        @Override
        protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
            return dispatcher instanceof CoherenceEventDispatcher
                    && (name == null || ((CoherenceEventDispatcher) dispatcher).getName().equals(name));
        }
    }


    /**
     * Handler for {@link SessionLifecycleEvent}s.
     */
    static class SessionLifecycleEventHandler extends EventHandler<SessionLifecycleEvent, SessionLifecycleEvent.Type> {

        private String name;

        SessionLifecycleEventHandler(ExecutableMethodEventObserver<SessionLifecycleEvent, ?, ?> observer) {
            super(observer, SessionLifecycleEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Starting) {
                    addType(SessionLifecycleEvent.Type.STARTING);
                } else if (a instanceof Started) {
                    addType(SessionLifecycleEvent.Type.STARTED);
                } else if (a instanceof Stopping) {
                    addType(SessionLifecycleEvent.Type.STOPPING);
                } else if (a instanceof Stopped) {
                    addType(SessionLifecycleEvent.Type.STOPPED);
                } else if (a instanceof Name) {
                    name = ((Name) a).value();
                } else if (a instanceof SessionName) {
                    name = ((SessionName) a).value();
                }
            }
        }

        @Override
        protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
            return dispatcher instanceof SessionEventDispatcher
                    && (name == null || ((SessionEventDispatcher) dispatcher).getName().equals(name));
        }
    }


    /**
     * Handler for {@link LifecycleEvent}s.
     */
    static class LifecycleEventHandler extends EventHandler<LifecycleEvent, LifecycleEvent.Type> {
        LifecycleEventHandler(ExecutableMethodEventObserver<LifecycleEvent, ?, ?> observer) {
            super(observer, LifecycleEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Activating) {
                    addType(LifecycleEvent.Type.ACTIVATING);
                } else if (a instanceof Activated) {
                    addType(LifecycleEvent.Type.ACTIVATED);
                } else if (a instanceof Disposing) {
                    addType(LifecycleEvent.Type.DISPOSING);
                }
            }
        }

        @Override
        boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
            return dispatcher instanceof ConfigurableCacheFactoryDispatcher;
        }

        @Override
        String getEventScope(LifecycleEvent event) {
            return event.getConfigurableCacheFactory().getScopeName();
        }
    }


    /**
     * Abstract base class for all observer-based cache interceptors.
     *
     * @param <E> the type of {@link Event} this interceptor accepts
     * @param <T> the enumeration of event types E supports
     */
    abstract static class CacheEventHandler<E extends Event<T>, T extends Enum<T>> extends EventHandler<E, T> {

        protected final String cacheName;

        protected final String serviceName;

        protected final String sessionName;

        CacheEventHandler(ExecutableMethodEventObserver<E, ?, ?> observer, Class<T> type) {
            super(observer, type);

            String cache = null;
            String service = null;
            String session = null;

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof CacheName) {
                    cache = ((CacheName) a).value();
                } else if (a instanceof MapName) {
                    cache = ((MapName) a).value();
                } else if (a instanceof ServiceName) {
                    service = ((ServiceName) a).value();
                } else if (a instanceof SessionName) {
                    session = ((SessionName) a).value();
                }
            }

            cacheName = cache;
            serviceName = service;
            sessionName = session;
        }

        @Override
        boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
            if (dispatcher instanceof CacheLifecycleEventDispatcher) {
                CacheLifecycleEventDispatcher cacheDispatcher = (CacheLifecycleEventDispatcher) dispatcher;

                if (scopeName == null || scopeName.equals(cacheDispatcher.getScopeName())) {
                    return ((cacheName == null || cacheName.equals(cacheDispatcher.getCacheName())) &&
                            (serviceName == null || serviceName.equals(removeScope(cacheDispatcher.getServiceName()))));
                }
            }

            return false;
        }
    }


    /**
     * Handler for {@link CacheLifecycleEvent}s.
     */
    static class CacheLifecycleEventHandler
            extends CacheEventHandler<CacheLifecycleEvent, CacheLifecycleEvent.Type> {

        CacheLifecycleEventHandler(ExecutableMethodEventObserver<CacheLifecycleEvent, ?, ?> observer) {
            super(observer, CacheLifecycleEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Created) {
                    addType(CacheLifecycleEvent.Type.CREATED);
                } else if (a instanceof Destroyed) {
                    addType(CacheLifecycleEvent.Type.DESTROYED);
                } else if (a instanceof Truncated) {
                    addType(CacheLifecycleEvent.Type.TRUNCATED);
                }
            }
        }
    }


    /**
     * Handler for {@link EntryEvent}s.
     *
     * @param <K> the type of the cache keys
     * @param <V> the type of the cache values
     */
    static class EntryEventHandler<K, V> extends CacheEventHandler<EntryEvent<K, V>, EntryEvent.Type> {

        EntryEventHandler(ExecutableMethodEventObserver<EntryEvent<K, V>, ?, ?> observer) {
            super(observer, EntryEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Inserting) {
                    addType(EntryEvent.Type.INSERTING);
                } else if (a instanceof Inserted) {
                    addType(EntryEvent.Type.INSERTED);
                } else if (a instanceof Updating) {
                    addType(EntryEvent.Type.UPDATING);
                } else if (a instanceof Updated) {
                    addType(EntryEvent.Type.UPDATED);
                } else if (a instanceof Removing) {
                    addType(EntryEvent.Type.REMOVING);
                } else if (a instanceof Removed) {
                    addType(EntryEvent.Type.REMOVED);
                }
            }
        }
    }


    /**
     * Handler for {@link EntryProcessorEvent}s.
     */
    static class EntryProcessorEventHandler
            extends CacheEventHandler<EntryProcessorEvent, EntryProcessorEvent.Type> {
        private final Class<?> m_classProcessor;

        EntryProcessorEventHandler(ExecutableMethodEventObserver<EntryProcessorEvent, ?, ?> observer) {
            super(observer, EntryProcessorEvent.Type.class);

            Class<?> classProcessor = null;

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Processor) {
                    classProcessor = ((Processor) a).value();
                } else if (a instanceof Executing) {
                    addType(EntryProcessorEvent.Type.EXECUTING);
                } else if (a instanceof Executed) {
                    addType(EntryProcessorEvent.Type.EXECUTED);
                }
            }

            m_classProcessor = classProcessor;
        }

        @Override
        boolean shouldFire(EntryProcessorEvent event) {
            return m_classProcessor == null || m_classProcessor.equals(event.getProcessor().getClass());
        }
    }


    /**
     * Abstract base class for all observer-based service interceptors.
     *
     * @param <E> the type of {@link Event} this interceptor accepts
     * @param <T> the enumeration of event types E supports
     */
    abstract static class ServiceEventHandler<E extends Event<T>, T extends Enum<T>>
            extends EventHandler<E, T> {

        protected final String serviceName;

        ServiceEventHandler(ExecutableMethodEventObserver<E, ?, ?> observer, Class<T> classType) {
            super(observer, classType);

            String service = null;

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof ServiceName) {
                    service = ((ServiceName) a).value();
                }
            }

            serviceName = service;
        }

        @Override
        protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
            if (dispatcher instanceof PartitionedServiceDispatcher) {
                PartitionedServiceDispatcher psd = (PartitionedServiceDispatcher) dispatcher;
                ConfigurableCacheFactory ccf = getConfigurableCacheFactory(psd.getService());

                if (ccf == null || scopeName == null || scopeName.equals(ccf.getScopeName())) {
                    return serviceName == null || serviceName.equals(removeScope(psd.getServiceName()));
                }
            }

            return false;
        }

        ConfigurableCacheFactory getConfigurableCacheFactory(PartitionedService service) {
            // a bit of a hack, but it should do the job
            if (service instanceof CacheService) {
                CacheService pc = (CacheService) service;
                return pc.getBackingMapManager().getCacheFactory();
            }
            return null;
        }
    }


    /**
     * Handler for {@link TransactionEvent}s.
     */
    static class TransactionEventHandler extends ServiceEventHandler<TransactionEvent, TransactionEvent.Type> {

        TransactionEventHandler(ExecutableMethodEventObserver<TransactionEvent, ?, ?> observer) {
            super(observer, TransactionEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Committing) {
                    addType(TransactionEvent.Type.COMMITTING);
                } else if (a instanceof Committed) {
                    addType(TransactionEvent.Type.COMMITTED);
                }
            }
        }
    }


    /**
     * Handler for {@link TransactionEvent}s.
     */
    static class TransferEventHandler extends ServiceEventHandler<TransferEvent, TransferEvent.Type> {

        TransferEventHandler(ExecutableMethodEventObserver<TransferEvent, ?, ?> observer) {
            super(observer, TransferEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Assigned) {
                    addType(TransferEvent.Type.ASSIGNED);
                } else if (a instanceof Arrived) {
                    addType(TransferEvent.Type.ARRIVED);
                } else if (a instanceof Departing) {
                    addType(TransferEvent.Type.DEPARTING);
                } else if (a instanceof Departed) {
                    addType(TransferEvent.Type.DEPARTED);
                } else if (a instanceof Lost) {
                    addType(TransferEvent.Type.LOST);
                } else if (a instanceof Recovered) {
                    addType(TransferEvent.Type.RECOVERED);
                } else if (a instanceof Rollback) {
                    addType(TransferEvent.Type.ROLLBACK);
                }
            }
        }
    }

    /**
     * Handler for {@link UnsolicitedCommitEvent}s.
     */
    public static class UnsolicitedCommitEventHandler
            extends ServiceEventHandler<UnsolicitedCommitEvent, UnsolicitedCommitEvent.Type> {
        public UnsolicitedCommitEventHandler(ExecutableMethodEventObserver<UnsolicitedCommitEvent, ?, ?> observer) {
            super(observer, UnsolicitedCommitEvent.Type.class);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Committed) {
                    addType(UnsolicitedCommitEvent.Type.COMMITTED);
                }
            }
        }
    }


    /**
     * Abstract base class for all observer-based federation interceptors.
     *
     * @param <E> the type of {@link com.tangosol.net.events.Event} this interceptor accepts
     * @param <T> the enumeration of event types E supports
     */
    abstract static class FederationEventHandler<E extends Event<T>, T extends Enum<T>> extends ServiceEventHandler<E, T> {

        protected final String participantName;
        protected final Function<E, String> participantNameFunction;

        FederationEventHandler(ExecutableMethodEventObserver<E, ?, ?> observer, Class<T> type,
                                         Function<E, String> participantNameFunction) {
            super(observer, type);

            this.participantNameFunction = participantNameFunction;

            String participantName = null;

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof ParticipantName) {
                    participantName = ((ParticipantName) a).value();
                }
            }

            this.participantName = participantName;
        }

        @Override
        @SuppressWarnings("rawtypes")
        protected boolean isApplicable(EventDispatcher dispatcher, String sScopeName) {
            Set<Enum> setSupported = dispatcher.getSupportedTypes();
            boolean fMatch = eventTypes().stream().anyMatch(setSupported::contains);
            return fMatch && super.isApplicable(dispatcher, sScopeName);
        }

        @Override
        protected boolean shouldFire(E event) {
            return participantName == null || participantName.equals(participantNameFunction.apply(event));
        }
    }

    /**
     * Handler for {@link FederatedConnectionEvent}s.
     */
    static class FederatedConnectionEventHandler
            extends FederationEventHandler<FederatedConnectionEvent, FederatedConnectionEvent.Type> {

        FederatedConnectionEventHandler(ExecutableMethodEventObserver<FederatedConnectionEvent, ?, ?> observer) {
            super(observer, FederatedConnectionEvent.Type.class, FederatedConnectionEvent::getParticipantName);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Connecting) {
                    addType(FederatedConnectionEvent.Type.CONNECTING);
                } else if (a instanceof Disconnected) {
                    addType(FederatedConnectionEvent.Type.DISCONNECTED);
                } else if (a instanceof Backlog) {
                    Backlog backlog = (Backlog) a;
                    if (backlog.value() == Backlog.Type.EXCESSIVE) {
                        addType(FederatedConnectionEvent.Type.BACKLOG_EXCESSIVE);
                    } else {
                        addType(FederatedConnectionEvent.Type.BACKLOG_NORMAL);
                    }
                } else if (a instanceof Error) {
                    addType(FederatedConnectionEvent.Type.ERROR);
                }
            }
        }
    }

    /**
     * Handler for {@link FederatedChangeEvent}s.
     */
    static class FederatedChangeEventHandler
            extends FederationEventHandler<FederatedChangeEvent, FederatedChangeEvent.Type> {

        FederatedChangeEventHandler(ExecutableMethodEventObserver<FederatedChangeEvent, ?, ?> observer) {
            super(observer, FederatedChangeEvent.Type.class, FederatedChangeEvent::getParticipant);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof CommittingLocal) {
                    addType(FederatedChangeEvent.Type.COMMITTING_LOCAL);
                } else if (a instanceof CommittingRemote) {
                    addType(FederatedChangeEvent.Type.COMMITTING_REMOTE);
                } else if (a instanceof Replicating) {
                    addType(FederatedChangeEvent.Type.REPLICATING);
                }
            }
        }
    }

    /**
     * Handler for {@link FederatedPartitionEvent}s.
     */
    static class FederatedPartitionEventHandler
            extends FederationEventHandler<FederatedPartitionEvent, FederatedPartitionEvent.Type> {

        FederatedPartitionEventHandler(ExecutableMethodEventObserver<FederatedPartitionEvent, ?, ?> observer) {
            super(observer, FederatedPartitionEvent.Type.class, FederatedPartitionEvent::getParticipant);

            for (Annotation a : observer.getObservedQualifiers()) {
                if (a instanceof Syncing) {
                    addType(FederatedPartitionEvent.Type.SYNCING);
                } else if (a instanceof Synced) {
                    addType(FederatedPartitionEvent.Type.SYNCED);
                }
            }
        }
    }
}
