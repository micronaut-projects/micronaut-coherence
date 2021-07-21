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
package io.micronaut.coherence;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.tangosol.net.*;
import io.micronaut.coherence.annotation.ExtractorBinding;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.View;

import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.*;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.util.StringUtils;
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
     * The micronaut bean context.
     */
    private final BeanContext beanContext;

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
    public NamedCacheFactories(BeanContext context, FilterFactories filters, ExtractorFactories extractors) {
        this.beanContext = context;
        this.filterFactory = filters;
        this.extractorFactory = extractors;
    }

    /**
     * The primary factory method for creating {@link NamedCache} beans.
     *
     * @param injectionPoint  the injection point to inject the cache into
     * @param <K>             the type of the cache keys
     * @param <V>             they type of the cache values
     *
     * @return  the required {@link NamedCache}
     */
    @Bean(preDestroy = "release", typed = NamedCache.class)
    @Prototype
    @Primary
    <K, V> NamedCache<K, V> getCache(InjectionPoint<?> injectionPoint) {
        return getCacheInternal(injectionPoint, false);
    }

    /**
     * A secondary cache factory method specifically for injection of {@link ContinuousQueryCache} beans.
     *
     * @param injectionPoint  the injection point to inject the cache into
     * @param <K>             the type of the cache keys
     * @param <V_BACK>        the type of the underlying cache values
     * @param <V_FRONT>       the type of the view cache values
     *
     * @return  the required {@link ContinuousQueryCache}
     */
    @Bean(preDestroy = "release", typed = ContinuousQueryCache.class)
    @Prototype
    @Secondary
    <K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getNamedView(InjectionPoint<?> injectionPoint) {
        return (ContinuousQueryCache<K, V_BACK, V_FRONT>) getCacheInternal(injectionPoint, true);
    }

    /**
     * A factory method specifically for injection of {@link AsyncNamedCache} beans.
     *
     * @param injectionPoint  the injection point to inject the cache into
     * @param <K>             the type of the cache keys
     * @param <V>             they type of the cache values
     *
     * @return  the required {@link AsyncNamedCache}
     */
    @Prototype
    <K, V> AsyncNamedCache<K, V> getAsyncCache(InjectionPoint<?> injectionPoint) {
        NamedCache<K, V> cache = getCacheInternal(injectionPoint, false);
        return cache.async();
    }

    private <K, V> NamedCache<K, V> getCacheInternal(InjectionPoint<?> injectionPoint, boolean isCQC) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        String sessionName = metadata.getValue(SessionName.class, String.class).orElse(Coherence.DEFAULT_NAME);
        String name = metadata.getValue(Name.class, String.class).orElse(getName(injectionPoint));

        isCQC = isCQC || injectionPoint.isAnnotationPresent(View.class);

        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException(
                    "Cannot determine cache/map name. No @Name qualifier and injection point is not named");
        }

        Session session = beanContext.createBean(Session.class, sessionName);

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
