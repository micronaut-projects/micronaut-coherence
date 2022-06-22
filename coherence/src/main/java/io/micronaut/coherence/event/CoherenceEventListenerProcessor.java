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

import com.oracle.coherence.common.base.Exceptions;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.internal.NamedEventInterceptor;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.SafeLinkedList;
import com.tangosol.util.filter.MapEventFilter;
import com.tangosol.util.filter.MapEventTransformerFilter;
import io.micronaut.coherence.FilterFactories;
import io.micronaut.coherence.MapEventTransformerFactories;
import io.micronaut.coherence.annotation.CoherenceEventListener;
import io.micronaut.coherence.annotation.Created;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@link ExecutableMethodProcessor} that processes methods annotated with
 * {@literal @}{@link io.micronaut.coherence.annotation.CoherenceEventListener}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
@Context
public class CoherenceEventListenerProcessor
        implements ExecutableMethodProcessor<CoherenceEventListener> {

    /**
     * The {@link io.micronaut.coherence.FilterFactories} instance used to create
     * {@link com.tangosol.util.Filter} instances.
     */
    private final FilterFactories filterProducer;

    /**
     * The {@link io.micronaut.coherence.MapEventTransformerFactories} instance used to create
     * {@link com.tangosol.util.MapEventTransformer} instances.
     */
    private final MapEventTransformerFactories transformerProducer;

    /**
     * A list of event interceptors for all discovered observer methods.
     */
    private final Map<String, Map<String, Set<AnnotatedMapListener<?, ?>>>> mapListeners = new HashMap<>();

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext ctx;

    /**
     * The event argument binder registry.
     */
    private final EventArgumentBinderRegistry<?> binderRegistry;

    @SuppressWarnings("unchecked")
    private final List<NamedEventInterceptor<?>> interceptors = new SafeLinkedList();

    /**
     * Create the {@link CoherenceEventListenerProcessor} bean.
     *
     * @param beanContext        the {@link io.micronaut.context.BeanContext}
     * @param filterFactories    the factory to produce {@link com.tangosol.util.Filter} instances
     * @param transformerFactory the factory to produce {@link com.tangosol.util.MapEventTransformer} instances
     */
    @Inject
    public CoherenceEventListenerProcessor(ApplicationContext beanContext,
                                           FilterFactories filterFactories,
                                           MapEventTransformerFactories transformerFactory) {
        this.filterProducer = filterFactories;
        this.transformerProducer = transformerFactory;
        this.ctx = beanContext;
        this.binderRegistry = new EventArgumentBinderRegistry<>();
    }

    /**
     * Returns the discovered interceptors.
     *
     * @return a list of discovered {@link NamedEventInterceptor} beans
     */
    public List<NamedEventInterceptor<?>> getInterceptors() {
        return interceptors;
    }

    /**
     * Process {@link io.micronaut.inject.ExecutableMethod} bean definitions for methods annotated with
     * {@link CoherenceEventListener}.
     * Each method will be turned into either an {@link com.tangosol.net.events.EventInterceptor} or a
     * {@link com.tangosol.util.MapListener}.
     *
     * @param beanDefinition The bean definition to process
     * @param method         The executable method
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        // must have a single parameter that is a Coherence event
        Argument<?>[] arguments = method.getArguments();
        Class<?> type = arguments.length == 1 ? arguments[0].getType() : null;
        if (type != null && (Event.class.isAssignableFrom(type) || MapEvent.class.isAssignableFrom(type))) {
            Class<?> clsBeanType = beanDefinition.getBeanType();
            Supplier<Object> bean = () -> ctx.getBean(clsBeanType);

            if (Event.class.isAssignableFrom(type)) {
                ExecutableMethodEventObserver observer = new ExecutableMethodEventObserver(bean, method, binderRegistry);
                EventObserverSupport.EventHandler handler = EventObserverSupport
                        .createObserver((Class<? extends Event>) type, observer);
                NamedEventInterceptor interceptor = new NamedEventInterceptor(observer.getId(), handler);
                interceptors.add(interceptor);
            } else {
                // type is MapEvent
                ExecutableMethodMapListener listener = new ExecutableMethodMapListener(bean, method, binderRegistry);
                AnnotatedMapListener mapListener = new AnnotatedMapListener(listener, listener.getObservedQualifiers());
                addMapListener(mapListener);
            }
        } else {
            throw new IllegalArgumentException("The @CoherenceEventListener annotated method "
                    + method
                    .getTargetMethod() + " must have a single Coherence Event or MapEvent argument.");
        }
    }

    /**
     * Listen for {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent.Type#CREATED Created}
     * {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent CacheLifecycleEvents}
     * and register relevant map listeners when caches are created.
     *
     * @param event the {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent}
     */
    @CoherenceEventListener
    @SuppressWarnings({"rawtypes", "unchecked"})
    void registerMapListeners(@Created CacheLifecycleEvent event) {
        String cacheName = event.getCacheName();
        String eventScope = event.getScopeName();
        String eventSession = event.getSessionName();
        String eventService = event.getServiceName();

        Set<AnnotatedMapListener<?, ?>> setListeners = getMapListeners(removeScope(eventService), cacheName);

        Session session = Coherence.findSession(eventSession)
                .orElseThrow(() -> new IllegalStateException("Cannot find a Session with name " + eventSession));
        NamedCache cache = session.getCache(cacheName);

        for (AnnotatedMapListener<?, ?> listener : setListeners) {
            if (listener.hasFilterAnnotation()) {
                // ensure that the listener's filter has been resolved as this
                // was not possible as discovery time.
                listener.resolveFilter(filterProducer);
            }

            if (listener.hasTransformerAnnotation()) {
                // ensure that the listener's transformer has been resolved as this
                // was not possible as discovery time.
                listener.resolveTransformer(transformerProducer);
            }

            String sScope = listener.getScopeName();
            boolean fScopeOK = sScope == null || sScope.equals(eventScope);
            String sSession = listener.getSessionName();
            boolean fSessionOK = sSession == null || sSession.equals(eventSession);

            if (fScopeOK && fSessionOK) {
                Filter filter = listener.getFilter();
                if (filter != null && !(filter instanceof MapEventFilter)) {
                    filter = new MapEventFilter(MapEventFilter.E_ALL, filter);
                }

                MapEventTransformer transformer = listener.getTransformer();
                if (transformer != null) {
                    filter = new MapEventTransformerFilter(filter, transformer);
                }

                try {
                    boolean fLite = listener.isLite();
                    if (listener.isSynchronous()) {
                        cache.addMapListener(listener.synchronous(), filter, fLite);
                    } else {
                        cache.addMapListener(listener, filter, fLite);
                    }
                } catch (Exception e) {
                    throw Exceptions.ensureRuntimeException(e);
                }
            }
        }
    }

    /**
     * Remove the scope prefix from a specified service name.
     *
     * @param sServiceName the service name to remove scope prefix from
     * @return service name with scope prefix removed
     */
    private String removeScope(String sServiceName) {
        if (sServiceName == null) {
            return "";
        }
        int nIndex = sServiceName.indexOf(':');
        return nIndex > -1 ? sServiceName.substring(nIndex + 1) : sServiceName;
    }

    /**
     * Add specified listener to the collection of discovered observer-based listeners.
     *
     * @param listener the listener to add
     */
    public void addMapListener(AnnotatedMapListener<?, ?> listener) {
        String svc = listener.getServiceName();
        String cache = listener.getCacheName();

        Map<String, Set<AnnotatedMapListener<?, ?>>> mapByCache = mapListeners.computeIfAbsent(svc, s -> new HashMap<>());
        Set<AnnotatedMapListener<?, ?>> setListeners = mapByCache.computeIfAbsent(cache, c -> new HashSet<>());
        setListeners.add(listener);
    }

    /**
     * Return all map listeners that should be registered for a particular
     * service and cache combination.
     *
     * @param serviceName the name of the service
     * @param cacheName   the name of the cache
     * @return a set of all listeners that should be registered
     */
    public Set<AnnotatedMapListener<?, ?>> getMapListeners(String serviceName, String cacheName) {
        HashSet<AnnotatedMapListener<?, ?>> setResults = new HashSet<>();
        collectMapListeners(setResults, "*", "*");
        collectMapListeners(setResults, "*", cacheName);
        collectMapListeners(setResults, serviceName, "*");
        collectMapListeners(setResults, serviceName, cacheName);

        return setResults;
    }

    /**
     * Return all map listeners that should be registered against a specific
     * remote cache or map in a specific session.
     *
     * @return all map listeners that should be registered against a
     * specific cache or map in a specific session
     */
    public Collection<AnnotatedMapListener<?, ?>> getNonWildcardMapListeners() {
        return mapListeners.values()
                .stream()
                .flatMap(map -> map.values().stream())
                .flatMap(Set::stream)
                .filter(listener -> listener.getSessionName() != null)
                .filter(listener -> !listener.isWildCardCacheName())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Add all map listeners for the specified service and cache combination to
     * the specified result set.
     *
     * @param setResults  the set of results to accumulate listeners into
     * @param serviceName the name of the service
     * @param cacheName   the name of the cache
     */
    private void collectMapListeners(HashSet<AnnotatedMapListener<?, ?>> setResults, String serviceName, String cacheName) {
        Map<String, Set<AnnotatedMapListener<?, ?>>> mapByCache = mapListeners.get(serviceName);
        if (mapByCache != null) {
            setResults.addAll(mapByCache.getOrDefault(cacheName, Collections.emptySet()));
        }
    }
}
