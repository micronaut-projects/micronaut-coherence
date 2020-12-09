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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import com.oracle.coherence.inject.ExtractorBinding;
import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;
import com.oracle.coherence.inject.View;

import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.InjectionPoint;

/**
 * A Micronaut factory for producing Coherence maps and caches and views.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
@SuppressWarnings({"rawtypes", "unchecked"})
class NamedCacheFactories {

    /**
     * The filter factory for use when creating views.
     */
    private final FilterFactories filterFactory;

    /**
     * The extractor factory for use when creating views.
     */
    private final ExtractorFactories extractorFactory;

    /**
     * A map of previously created views.
     */
    private final Map<ViewId, WeakReference<ContinuousQueryCache>> views = new ConcurrentHashMap<>();

    @Inject
    public NamedCacheFactories(FilterFactories filters, ExtractorFactories extractors) {
        this.filterFactory    = filters;
        this.extractorFactory = extractors;
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Named("View")
    @Type(ContinuousQueryCache.class)
    <K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getView(InjectionPoint<?> injectionPoint) {
        return (ContinuousQueryCache<K, V_BACK, V_FRONT>) getCacheInternal(injectionPoint, true);
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Named("Name")
    @Type(ContinuousQueryCache.class)
    @Secondary
    <K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getNamedView(InjectionPoint<?> injectionPoint) {
        return (ContinuousQueryCache<K, V_BACK, V_FRONT>) getCacheInternal(injectionPoint, true);
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Named("SessionName")
    @Type(NamedCache.class)
    <K, V> NamedCache<K, V> getCacheForSession(InjectionPoint<?> injectionPoint) {
        return getCache(injectionPoint);
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Named("Name")
    @Type(NamedCache.class)
    @Primary
    <K, V> NamedCache<K, V> getCache(InjectionPoint<?> injectionPoint) {
        return getCacheInternal(injectionPoint, false);
    }

    private <K, V> NamedCache<K, V> getCacheInternal(InjectionPoint<?> injectionPoint, boolean isCQC) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        String sessionName = metadata.getValue(SessionName.class, String.class).orElse(Coherence.DEFAULT_NAME);
        String name = metadata.getValue(Name.class, String.class).orElse(getName(injectionPoint));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine cache/map name. No @Name qualifier and injection point is not named");
        }

        Session session = Coherence.findSession(sessionName)
                .orElseThrow(() -> new IllegalStateException("No Session is configured with name " + sessionName));

        NamedCache<K, V> cache = session.getCache(name);

        if (isCQC || metadata.hasAnnotation(View.class)) {
            boolean hasValues = metadata.booleanValue(View.class, "cacheValues").orElse(true);
            Filter filter = filterFactory.filter(injectionPoint);
            ValueExtractor extractor = getExtractor(injectionPoint);
            ViewId id = new ViewId(name, sessionName, filter, hasValues, extractor);

            WeakReference<ContinuousQueryCache> refCQC = views.compute(id, (key, ref) -> {
                ContinuousQueryCache cqc = ref == null ? null : ref.get();
                if (cqc == null || !cqc.isActive()) {
                    cqc = new ContinuousQueryCache<>(cache, filter, hasValues, null, extractor);
                    return new WeakReference<>(cqc);
                } else {
                    return ref;
                }
            });

            return refCQC.get();
        } else {
            return cache;
        }
    }

    @Prototype
    @Named("Name")
    @Primary
    <K, V> AsyncNamedCache<K, V> getAsyncCache(InjectionPoint<?> injectionPoint) {
        NamedCache<K, V> cache = getCache(injectionPoint);
        return cache.async();
    }

    @Prototype
    @Named("SessionName")
    <K, V> AsyncNamedCache<K, V> getAsyncCacheForSession(InjectionPoint<?> injectionPoint) {
        NamedCache<K, V> cache = getCache(injectionPoint);
        return cache.async();
    }

    /**
     * Returns the name of an injection point.
     *
     * @param injectionPoint the injection point to find the name of
     *
     * @return the name of an injection point
     */
    private String getName(InjectionPoint<?> injectionPoint) {
        if (injectionPoint instanceof io.micronaut.core.naming.Named) {
            return ((io.micronaut.core.naming.Named) injectionPoint).getName();
        }
        return null;
    }

    /**
     * If the injection point is annotated with an annotation with a stereotype
     * of {@link ExtractorBinding} then return the corresponding {@link ValueExtractor}
     * otherwise return {@code null}.
     *
     * @param injectionPoint  the injection point to create the extractor for
     *
     * @return  the {@link ValueExtractor} from the annotated injection point
     *          or {@code null} if there are no extractor annotations
     */
    private ValueExtractor<?, ?> getExtractor(InjectionPoint<?> injectionPoint) {
        if (injectionPoint.getAnnotationMetadata().hasStereotype(ExtractorBinding.class)) {
            return extractorFactory.extractor(injectionPoint);
        }
        return null;
    }

    /**
     * An identifier for a cache view.
     */
    static class ViewId {
        private final String name;
        private final String sessionName;
        private final Filter<?> filter;
        private final boolean hasValues;
        private final ValueExtractor<?, ?> extractor;

        ViewId(String name, String sessionName, Filter<?> filter, boolean hasValues, ValueExtractor<?, ?> extractor) {
            this.name = name;
            this.sessionName = sessionName;
            this.filter = filter;
            this.hasValues = hasValues;
            this.extractor = extractor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ViewId viewId = (ViewId) o;
            return hasValues == viewId.hasValues &&
                    Objects.equals(name, viewId.name) &&
                    Objects.equals(sessionName, viewId.sessionName) &&
                    Objects.equals(filter, viewId.filter) &&
                    Objects.equals(extractor, viewId.extractor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, sessionName, filter, hasValues, extractor);
        }
    }
}
