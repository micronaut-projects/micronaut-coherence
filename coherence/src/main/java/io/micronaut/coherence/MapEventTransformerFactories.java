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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.micronaut.coherence.annotation.ExtractorBinding;
import io.micronaut.coherence.annotation.MapEventTransformerBinding;

import com.tangosol.util.MapEventTransformer;

import com.tangosol.util.ValueExtractor;
import com.tangosol.util.transformer.ExtractorEventTransformer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.InjectionPoint;
import jakarta.inject.Inject;

/**
 * A Micronaut factory for producing {@link com.tangosol.util.MapEventTransformer} instances.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
public class MapEventTransformerFactories {

    /**
     * The Micronaut bean context.
     */
    protected final ApplicationContext ctx;

    /**
     * The extractor factory for use when creating views.
     */
    protected final ExtractorFactories extractorFactory;

    /**
     * Create a {@link MapEventTransformerFactories}.
     *
     * @param ctx the Micronaut bean context
     * @param extractorFactory  the factory to use to produce {@link ValueExtractor ValueExtractors}
     */
    @Inject
    MapEventTransformerFactories(ApplicationContext ctx, ExtractorFactories extractorFactory) {
        this.ctx = ctx;
        this.extractorFactory = extractorFactory;
    }

    /**
     * Resolve a {@link com.tangosol.util.MapEventTransformer} from the
     * specified qualifier annotations.
     *
     * @param annotations  the qualifier annotations to use to create the transformer
     * @param <K>          the type of the keys of the entry to be transformed
     * @param <V>          the type of the values of the entry to be transformed
     * @param <U>          the type of the transformed values
     *
     * @return a {@link com.tangosol.util.MapEventTransformer} from the
     *         specified qualifier annotations
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <K, V, U> MapEventTransformer<K, V, U> resolve(Set<Annotation> annotations) {
        Optional<Annotation> optionalTransformer = annotations.stream()
                .filter(a -> a.annotationType().isAnnotationPresent(MapEventTransformerBinding.class))
                .findFirst();
        Optional<Annotation> optionalExtractor = annotations.stream()
                .filter(a -> a.annotationType().isAnnotationPresent(ExtractorBinding.class))
                .findFirst();

        if (optionalTransformer.isPresent()) {
            Annotation annotation = optionalTransformer.get();
            Class<? extends Annotation> type = annotation.annotationType();
            MapEventTransformerFactory factory
                    = ctx.findBean(MapEventTransformerFactory.class, new FactoryQualifier<>(type)).orElse(null);
            if (factory != null) {
                return factory.create(annotation);
            } else {
                throw new IllegalStateException(
                        "Unsatisfied dependency - no MapEventTransformerFactory bean found annotated with " + annotation);
            }
        } else if (optionalExtractor.isPresent()) {
             // there is one or more ExtractorBinding annotations
             ValueExtractor<Object, Object> extractor = extractorFactory.resolve(annotations);
             return new ExtractorEventTransformer(extractor);
        }

        // there are no transformer or extractor annotations.
        return null;
    }

    /**
     * Create a {@link MapEventTransformer} for the specified injection point.
     *
     * @param injectionPoint  the injection point to inject a {@link MapEventTransformer} into
     *
     * @return a {@link MapEventTransformer} for the specified injection point
     */
    @Prototype
    @SuppressWarnings({"rawtypes", "unchecked"})
    MapEventTransformer transformer(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        List<Class<? extends Annotation>> bindings = metadata.getAnnotationTypesByStereotype(MapEventTransformerBinding.class);

        for (Class<? extends Annotation> type : bindings) {
            MapEventTransformerFactory factory
                    = ctx.findBean(MapEventTransformerFactory.class, new FactoryQualifier<>(type)).orElse(null);
            if (factory != null) {
                return factory.create(injectionPoint.synthesize(type));
            }
        }

        throw new IllegalStateException(
                "Unsatisfied dependency - no MapEventTransformerFactory bean found for bindings " + bindings);
    }
}
