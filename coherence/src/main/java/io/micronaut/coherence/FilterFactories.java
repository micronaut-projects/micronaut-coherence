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

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.AlwaysFilter;
import io.micronaut.coherence.annotation.FilterBinding;
import io.micronaut.coherence.annotation.WhereFilter;

import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.QueryHelper;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.InjectionPoint;

/**
 * A Micronaut factory for producing {@link com.tangosol.util.Filter} instances.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
public class FilterFactories {

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext ctx;

    /**
     * Create a {@link FilterFactories} that will use the specified bean context.
     *
     * @param ctx the bean context to use
     */
    @Inject
    FilterFactories(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Produce a {@link FilterFactory} that produces an instance of an
     * {@link com.tangosol.util.filter.AlwaysFilter}.
     *
     * @return a {@link FilterFactory} that produces an instance of an
     *         {@link com.tangosol.util.filter.AlwaysFilter}
     */
    @Singleton
    @AlwaysFilter
    FilterFactory<AlwaysFilter, ?> alwaysFactory() {
        return annotation -> Filters.always();
    }

    /**
     * Produce a {@link FilterFactory} that produces an instance of a
     * {@link com.tangosol.util.Filter} created from a CohQL where clause.
     *
     * @return a {@link FilterFactory} that produces an instance of an
     *         {@link com.tangosol.util.Filter} created from a CohQL
     *         where clause
     */
    @Singleton
    @WhereFilter("")
    @SuppressWarnings("unchecked")
    FilterFactory<WhereFilter, ?> whereFactory() {
        return annotation -> {
            String sWhere = annotation.value();
            return sWhere.trim().isEmpty() ? Filters.always() : QueryHelper.createFilter(annotation.value());
        };
    }

    /**
     * Create a {@link Filter} bean based on the annotations present
     * on an injection point.
     *
     * @param injectionPoint  the {@link io.micronaut.inject.InjectionPoint} to
     *                        create the {@link Filter} for
     * @return a {@link Filter} bean based on the annotations present
     *         on the injection point
     */
    @Prototype
    @SuppressWarnings({"rawtypes", "unchecked"})
    Filter<?> filter(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        List<Class<? extends Annotation>> bindings = metadata.getAnnotationTypesByStereotype(FilterBinding.class);

        for (Class<? extends Annotation> type : bindings) {
            Repeatable repeatable = type.getAnnotation(Repeatable.class);
            if (repeatable != null) {
                type = repeatable.value();
            }
            FilterFactory filterFactory = ctx.findBean(FilterFactory.class, new FactoryQualifier<>(type))
                    .orElse(null);
            if (filterFactory != null) {
                return filterFactory.create(injectionPoint.synthesize(type));
            }
        }

        return Filters.always();
    }

    /**
     * Resolve a {@link Filter} implementation from the specified qualifiers.
     *
     * @param annotations  the qualifiers to use to create the {@link Filter}
     * @param <T>          the type that the {@link Filter} can filter
     *
     * @return a {@link Filter} implementation created from the specified qualifiers.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Filter<T> resolve(Set<Annotation> annotations) {
        List<Filter<?>> list = new ArrayList<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.isAnnotationPresent(FilterBinding.class)) {
                FilterFactory factory = ctx.findBean(FilterFactory.class, new FactoryQualifier<>(type)).orElse(null);
                if (factory != null) {
                    Filter filter = factory.create(annotation);
                    if (filter != null) {
                        list.add(filter);
                    }
                } else {
                    throw new IllegalStateException("Unsatisfied dependency - no FilterFactory bean found annotated with " + annotation);
                }
            }
        }

        Filter[] aFilters = list.toArray(new Filter[0]);

        if (aFilters.length == 0) {
            return Filters.always();
        } else if (aFilters.length == 1) {
            return aFilters[0];
        } else {
            return Filters.all(aFilters);
        }
    }
}
