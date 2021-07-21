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


import io.micronaut.coherence.annotation.ChainedExtractor;
import io.micronaut.coherence.annotation.ExtractorBinding;
import io.micronaut.coherence.annotation.PofExtractor;
import io.micronaut.coherence.annotation.PropertyExtractor;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.InjectionPoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * A Micronaut factory for producing {@link com.tangosol.util.ValueExtractor} instances.
 *
 * @author Vaso Putica
 * @since 1.0
 */
@Factory
public class ExtractorFactories {

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext ctx;

    /**
     * Create a {@link ExtractorFactories} that will use the specified bean context.
     *
     * @param ctx the bean context to use
     */
    @Inject
    ExtractorFactories(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create a {@link ValueExtractor} bean based on the annotations present
     * on an injection point.
     *
     * @param injectionPoint the {@link io.micronaut.inject.InjectionPoint} to
     *                       create the {@link ValueExtractor} for
     * @return a {@link ValueExtractor} bean based on the annotations present
     * on the injection point
     */
    @Prototype
    @SuppressWarnings({"rawtypes", "unchecked"})
    ValueExtractor<?, ?> extractor(InjectionPoint<?> injectionPoint) {
        List<ValueExtractor> list = new ArrayList<>();

        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        List<Class<? extends Annotation>> bindings = metadata.getAnnotationTypesByStereotype(ExtractorBinding.class);

        for (Class<? extends Annotation> type : bindings) {
            Repeatable repeatable = type.getAnnotation(Repeatable.class);
            if (repeatable != null) {
                type = repeatable.value();
            }
            ExtractorFactory extractorFactory = ctx.findBean(ExtractorFactory.class, new FactoryQualifier<>(type))
                    .orElse(null);
            if (extractorFactory == null) {
                throw new IllegalStateException("Unsatisfied dependency - no ExtractorFactory bean found annotated with " + type);
            }

            ValueExtractor extractor = extractorFactory.create(injectionPoint.synthesize(type));
            if (extractor == null) {
                throw new IllegalStateException("Unsatisfied dependency - no extractor could be created by "
                        + extractorFactory + " extractor factory.");
            }
            list.add(extractor);
        }

        ValueExtractor[] aExtractors = list.toArray(new ValueExtractor[0]);
        if (aExtractors.length == 0) {
            throw new IllegalStateException("Unsatisfied dependency - no ExtractorFactory bean found annotated with " + bindings);
        } else if (aExtractors.length == 1) {
            return aExtractors[0];
        } else {
            return Extractors.multi(aExtractors);
        }
    }

    /**
     * Resolve a {@link ValueExtractor} implementation from the specified qualifiers.
     *
     * @param annotations  the qualifiers to use to create the {@link ValueExtractor}
     * @param <T>          the type that the {@link ValueExtractor} can extract from
     * @param <E>          the type that the {@link ValueExtractor} extracts
     *
     * @return a {@link ValueExtractor} implementation created from the specified qualifiers.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, E> ValueExtractor<T, E> resolve(Set<Annotation> annotations) {
        List<ValueExtractor> list = new ArrayList<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            ExtractorFactory extractorFactory = ctx.findBean(ExtractorFactory.class, new FactoryQualifier<>(type))
                    .orElse(null);
            if (extractorFactory == null) {
                throw new IllegalStateException("Unsatisfied dependency - no ExtractorFactory bean found annotated with " + type);
            }

            ValueExtractor extractor = extractorFactory.create(annotation);
            if (extractor == null) {
                throw new IllegalStateException("Unsatisfied dependency - no extractor could be created by "
                        + extractorFactory + " extractor factory.");
            }
            list.add(extractor);
        }

        ValueExtractor[] aExtractors = list.toArray(new ValueExtractor[0]);
        if (aExtractors.length == 0) {
            return null;
        } else if (aExtractors.length == 1) {
            return aExtractors[0];
        } else {
            return Extractors.multi(aExtractors);
        }
    }

    /**
     * A {@link ExtractorFactory} that produces {@link ValueExtractor}
     * instances for a given property or method name.
     *
     * @return {@link ExtractorFactory} that produces an instance of an
     * {@link ValueExtractor} for a given property or method name.
     */
    @Singleton
    @PropertyExtractor("")
    ExtractorFactory<PropertyExtractor, Object, Object> universalExtractor() {
        return annotation -> Extractors.extract(annotation.value());
    }

    /**
     * A {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link PropertyExtractor.Extractors} annotation.
     *
     * @return a {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link PropertyExtractor.Extractors} annotation.
     */
    @Singleton
    @PropertyExtractor.Extractors({})
    @SuppressWarnings({"unchecked", "rawtypes"})
    ExtractorFactory<PropertyExtractor.Extractors, ?, ?> universalExtractors() {
        return annotation -> {
            ValueExtractor[] extractors = Arrays.stream(annotation.value())
                    .map(ann -> Extractors.extract(ann.value()))
                    .toArray(ValueExtractor[]::new);
            return extractors.length == 1 ? extractors[0] : Extractors.multi(extractors);
        };
    }

    /**
     * A {@link ExtractorFactory} that produces chained {@link
     * ValueExtractor} instances for an array of property or method names.
     *
     * @return a {@link ExtractorFactory} that produces chained {@link
     * ValueExtractor} instances for an array of property or method names.
     */
    @Singleton
    @ChainedExtractor("")
    ExtractorFactory<ChainedExtractor, ?, ?> chainedExtractor() {
        return annotation -> Extractors.chained(annotation.value());
    }


    /**
     * A {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link ChainedExtractor.Extractors} annotation.
     *
     * @return a {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link ChainedExtractor.Extractors} annotation.
     */
    @Singleton
    @ChainedExtractor.Extractors({})
    @SuppressWarnings({"unchecked", "rawtypes"})
    ExtractorFactory<ChainedExtractor.Extractors, ?, ?> chainedExtractors() {
        return annotation -> {
            ValueExtractor[] extractors = Arrays.stream(annotation.value())
                    .map(ann -> Extractors.chained(ann.value()))
                    .toArray(ValueExtractor[]::new);
            return extractors.length == 1 ? extractors[0] : Extractors.multi(extractors);
        };
    }

    /**
     * A {@link ExtractorFactory} that produces{@link ValueExtractor}
     * instances for a given POF index or property path.
     *
     * @return a {@link ExtractorFactory} that produces{@link ValueExtractor}
     * instances for a given POF index or property path.
     */
    @Singleton
    @PofExtractor()
    @SuppressWarnings({"unchecked", "rawtypes"})
    ExtractorFactory<PofExtractor, ?, ?> pofExtractor() {
        return annotation -> {
            Class clazz = annotation.type().equals(Object.class)
                    ? null
                    : annotation.type();
            String sPath = annotation.path();
            int[] anIndex = annotation.index();

            if (sPath.length() == 0 && anIndex.length == 0) {
                throw new IllegalArgumentException("Neither 'index' nor 'path' are defined within @PofExtractor annotation. One is required.");
            }
            if (sPath.length() > 0 && anIndex.length > 0) {
                throw new IllegalArgumentException("Both 'index' and 'path' are defined within @PofExtractor annotation. Only one is allowed.");
            }
            if (sPath.length() > 0 && clazz == null) {
                throw new IllegalArgumentException("'type' must be specified within @PofExtractor annotation when property path is used.");
            }

            return sPath.length() > 0
                    ? Extractors.fromPof(clazz, sPath)
                    : Extractors.fromPof(clazz, anIndex);
        };
    }

    /**
     * A {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link PofExtractor.Extractors} annotation.
     *
     * @return a {@link ExtractorFactory} that produces {@link
     * com.tangosol.util.extractor.MultiExtractor} containing {@link
     * ValueExtractor} instances produced from the annotations contained in a
     * {@link PofExtractor.Extractors} annotation.
     */
    @Singleton
    @PofExtractor.Extractors({})
    @SuppressWarnings({"unchecked", "rawtypes"})
    ExtractorFactory<PofExtractor.Extractors, ?, ?> pofExtractors() {
        final ExtractorFactory<PofExtractor, ?, ?> factory = pofExtractor();
        return annotation -> {
            ValueExtractor[] extractors = Arrays.stream(annotation.value())
                    .map(factory::create)
                    .toArray(ValueExtractor[]::new);
            return extractors.length == 1 ? extractors[0] : Extractors.multi(extractors);
        };
    }
}
