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

import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.MapListener;
import com.tangosol.util.comparator.SafeComparator;
import com.tangosol.util.function.Remote;
import io.micronaut.coherence.FilterFactories;
import io.micronaut.coherence.MapEventTransformerFactories;
import io.micronaut.coherence.annotation.*;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * {@link MapListener} implementation that dispatches {@code MapEvent}s
 * to {@link io.micronaut.coherence.annotation.CoherenceEventListener}
 * annotated methods.
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cache value
 * @author Jonathan Knight
 * @since 1.0
 */
class AnnotatedMapListener<K, V> implements MapListener<K, V>, Comparable<AnnotatedMapListener<?, ?>> {

    /**
     * The wild-card value for cache and service names.
     */
    public static final String WILD_CARD = "*";

    /**
     * The event observer for this listener.
     */
    private final ExecutableMethodMapListener<K, V, ?, ?> observer;

    /**
     * The name of the cache to observe map events for.
     */
    private final String cacheName;

    /**
     * The name of the cache service owing the cache to observe map events for.
     */
    private final String serviceName;

    /**
     * The scope name of the cache factory owning the cache to observer map events for.
     */
    private final String scopeName;

    /**
     * The types of map event to observe.
     */
    private final EnumSet<Type> eventTypes = EnumSet.noneOf(Type.class);

    /**
     * The optional annotation specifying the filter to use to filter events.
     */
    private final Set<Annotation> filterAnnotations;

    /**
     * The optional annotations specifying the map event transformers to use to
     * transform observed map events.
     */
    private final Set<Annotation> transformerAnnotations;

    /**
     * The optional annotations specifying the value extractors to use to
     * transform observed map events.
     */
    private final Set<Annotation> extractorAnnotations;

    /**
     * The name of the session if this listener is for a resource
     * managed by a specific session or {@code null} if this listener
     * is for a resource in any session.
     */
    private String session;

    /**
     * A flag indicating whether to subscribe to lite-events.
     */
    private boolean liteEvents;

    /**
     * A flag indicating whether the observer is synchronous.
     */
    private boolean synchronousEvents;

    /**
     * An optional {@link Filter} to use to filter observed map events.
     */
    private Filter<?> filter;

    /**
     * An optional {@link MapEventTransformer} to use to transform observed map events.
     */
    private MapEventTransformer<K, V, ?> transformer;

    AnnotatedMapListener(ExecutableMethodMapListener<K, V, ?, ?> observer, Set<Annotation> annotations) {
        this.observer = observer;

        String cacheName = WILD_CARD;
        String serviceName = WILD_CARD;
        String scopeName = null;

        for (Annotation a : observer.getObservedQualifiers()) {
            if (a instanceof CacheName) {
                cacheName = ((CacheName) a).value();
            } else if (a instanceof MapName) {
                cacheName = ((MapName) a).value();
            } else if (a instanceof ServiceName) {
                serviceName = ((ServiceName) a).value();
            } else if (a instanceof ScopeName) {
                scopeName = ((ScopeName) a).value();
            } else if (a instanceof Inserted) {
                addType(Type.INSERTED);
            } else if (a instanceof Updated) {
                addType(Type.UPDATED);
            } else if (a instanceof Deleted) {
                addType(Type.DELETED);
            } else if (a instanceof SessionName) {
                session = ((SessionName) a).value();
            }
        }

        if (annotations.contains(Lite.Literal.INSTANCE)) {
            liteEvents = true;
        }
        if (annotations.contains(Synchronous.Literal.INSTANCE)) {
            synchronousEvents = true;
        }

        filterAnnotations = annotations.stream()
                .filter(a -> a.annotationType().isAnnotationPresent(FilterBinding.class))
                .collect(Collectors.toSet());

        extractorAnnotations = annotations.stream()
                .filter(a -> a.annotationType().isAnnotationPresent(ExtractorBinding.class))
                .collect(Collectors.toSet());

        transformerAnnotations = annotations.stream()
                .filter(a -> a.annotationType().isAnnotationPresent(MapEventTransformerBinding.class))
                .collect(Collectors.toSet());

        this.cacheName = cacheName;
        this.serviceName = serviceName;
        this.scopeName = scopeName;
    }

    @Override
    public void entryInserted(MapEvent<K, V> event) {
        handle(Type.INSERTED, event);
    }

    @Override
    public void entryUpdated(MapEvent<K, V> event) {
        handle(Type.UPDATED, event);
    }

    @Override
    public void entryDeleted(MapEvent<K, V> event) {
        handle(Type.DELETED, event);
    }

    @Override
    public int compareTo(AnnotatedMapListener<?, ?> other) {
        int result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.session, other.session);
        if (result == 0) {
            result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.cacheName, other.cacheName);
            if (result == 0) {
                result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.serviceName, other.serviceName);
            }
        }
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AnnotatedMapListener<?, ?> that)) {
            return false;
        }

    return liteEvents == that.liteEvents && synchronousEvents == that.synchronousEvents &&
                Objects.equals(observer, that.observer) &&
                Objects.equals(getCacheName(), that.getCacheName()) &&
                Objects.equals(getServiceName(), that.getServiceName()) &&
                Objects.equals(getScopeName(), that.getScopeName()) &&
                Objects.equals(eventTypes, that.eventTypes) &&
                Objects.equals(filterAnnotations, that.filterAnnotations) &&
                Objects.equals(transformerAnnotations, that.transformerAnnotations) &&
                Objects.equals(extractorAnnotations, that.extractorAnnotations) &&
                Objects.equals(session, that.session) &&
                Objects.equals(getFilter(), that.getFilter()) &&
                Objects.equals(getTransformer(), that.getTransformer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(observer, getCacheName(), getServiceName(), getScopeName(), eventTypes, filterAnnotations,
                transformerAnnotations, extractorAnnotations, session, liteEvents, synchronousEvents,
                getFilter(), getTransformer());
    }

    /**
     * Return the name of the session that this listener is for.
     *
     * @return the name of the session this listener is for
     */
    public String getSessionName() {
        return session;
    }

    /**
     * Returns {@code true} if this listener has a filter annotation to resolve.
     *
     * @return {@code true} if this listener has a filter annotation to resolve
     */
    public boolean hasFilterAnnotation() {
        return filterAnnotations != null && !filterAnnotations.isEmpty();
    }

    /**
     * Resolve this listener's filter annotation into a {@link Filter} instance.
     * <p>
     * If this listener's filter has already been resolved this operation is a no-op.
     *
     * @param producer the {@link io.micronaut.coherence.FilterFactories} to use to resolve the {@link Filter}
     */
    public void resolveFilter(FilterFactories producer) {
        if (filter == null && filterAnnotations != null && !filterAnnotations.isEmpty()) {
            filter = producer.resolve(filterAnnotations);
        }
    }

    /**
     * Returns {@code true} if this listener has a transformer annotation to resolve.
     *
     * @return {@code true} if this listener has a transformer annotation to resolve
     */
    public boolean hasTransformerAnnotation() {
        return !transformerAnnotations.isEmpty() || !extractorAnnotations.isEmpty();
    }

    /**
     * Resolve this listener's transformer annotation into a {@link MapEventTransformer} instance.
     * <p>
     * If this listener's transformer has already been resolved this method is a no-op
     *
     * @param producer the {@link io.micronaut.coherence.MapEventTransformerFactories} to use to resolve
     *                 the {@link MapEventTransformer}
     */
    public void resolveTransformer(MapEventTransformerFactories producer) {
        if (transformer != null) {
            return;
        }

        if (!transformerAnnotations.isEmpty()) {
            transformer = producer.resolve(transformerAnnotations);
        } else if (!extractorAnnotations.isEmpty()) {
            transformer = producer.resolve(extractorAnnotations);
        }
    }

    /**
     * Obtain the {@link Filter} that should be used when registering this listener.
     *
     * @return the {@link Filter} that should be used when registering this listener
     */
    public Filter<?> getFilter() {
        return filter;
    }

    /**
     * Obtain the {@link MapEventTransformer} that should be used when registering this listener.
     *
     * @return the {@link MapEventTransformer} that should be used when registering this listener
     */
    @SuppressWarnings("rawtypes")
    public MapEventTransformer getTransformer() {
        return transformer;
    }

    /**
     * Return the name of the cache this listener is for, or {@code '*'} if
     * it should be registered regardless of the cache name.
     *
     * @return the name of the cache this listener is for
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Return {@code true} if this listener is for a wild-card cache name.
     *
     * @return {@code true} if this listener is for a wild-card cache name
     */
    public boolean isWildCardCacheName() {
        return WILD_CARD.equals(cacheName);
    }

    /**
     * Return the name of the service this listener is for, or {@code '*'} if
     * it should be registered regardless of the service name.
     *
     * @return the name of the cache this listener is for
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Return {@code true} if this listener is for a wild-card cache name.
     *
     * @return {@code true} if this listener is for a wild-card cache name
     */
    public boolean isWildCardServiceName() {
        return WILD_CARD.equals(serviceName);
    }

    /**
     * Return the name of the scope this listener is for, or {@code null} if
     * it should be registered regardless of the scope name.
     *
     * @return the name of the cache this listener is for
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * Return {@code true} if this is lite event listener.
     *
     * @return {@code true} if this is lite event listener
     */
    public boolean isLite() {
        return liteEvents;
    }

    /**
     * Return {@code true} if this is synchronous event listener.
     *
     * @return {@code true} if this is synchronous event listener
     */
    @Override
    public boolean isSynchronous() {
        return synchronousEvents;
    }

    /**
     * Add specified event type to a set of types this interceptor should handle.
     *
     * @param type the event type to add
     */
    private void addType(Type type) {
        eventTypes.add(type);
    }

    /**
     * Return {@code true} if this listener should handle events of the specified
     * type.
     *
     * @param type the type to check
     * @return {@code true} if this listener should handle events of the specified
     * type
     */
    private boolean isSupported(Type type) {
        return eventTypes.isEmpty() || eventTypes.contains(type);
    }

    /**
     * Notify the observer that the specified event occurred, if the event type
     * is supported.
     *
     * @param type  the event type
     * @param event the event
     */
    private void handle(Type type, MapEvent<K, V> event) {
        if (isSupported(type)) {
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

    @Override
    public String toString() {
        return "AnnotatedMapListener{" +
                "cacheName='" + cacheName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", scopeName='" + scopeName + '\'' +
                ", session='" + session + '\'' +
                ", observer='" + observer + '\'' +
                '}';
    }

    /**
     * Event type enumeration.
     */
    enum Type {
        INSERTED,
        UPDATED,
        DELETED
    }
}
