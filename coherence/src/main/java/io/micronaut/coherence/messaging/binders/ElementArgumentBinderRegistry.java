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
package io.micronaut.coherence.messaging.binders;

import com.tangosol.net.topic.Subscriber;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An registry of {@link ElementBinder} instances.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
public class ElementArgumentBinderRegistry
        implements ArgumentBinderRegistry<Subscriber.Element<?>> {

    private final Map<Class<? extends Annotation>, ElementBinder<?>> byAnnotation = new HashMap<>();
    private final Map<Integer, ElementBinder<?>> byType = new HashMap<>();

    /**
     * Creates the registry for the given binders.
     *
     * @param binders The binders
     */
    @SuppressWarnings("rawtypes")
    public ElementArgumentBinderRegistry(ElementBinder<?>... binders) {
        if (ArrayUtils.isNotEmpty(binders)) {
            for (ElementBinder<?> binder : binders) {
                if (binder instanceof AnnotatedElementBinder) {
                    AnnotatedElementBinder<?, ?> annotatedElementBinder = (AnnotatedElementBinder<?, ?>) binder;
                    byAnnotation.put(
                            annotatedElementBinder.annotationType(),
                            annotatedElementBinder
                    );
                } else if (binder instanceof TypedElementBinder) {
                    TypedElementBinder typedElementBinder = (TypedElementBinder) binder;
                    byType.put(
                            typedElementBinder.argumentType().typeHashCode(),
                            typedElementBinder
                    );
                }
            }
        }
    }

    @Override
    public <T> Optional<ArgumentBinder<T, Subscriber.Element<?>>> findArgumentBinder(Argument<T> argument, Subscriber.Element<?> source) {
        Optional<Class<? extends Annotation>> annotationType = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
        if (annotationType.isPresent()) {
            @SuppressWarnings("unchecked") ElementBinder<T> elementBinder =
                    (ElementBinder<T>) byAnnotation.get(annotationType.get());

            return Optional.ofNullable(elementBinder);
        } else {
            @SuppressWarnings("unchecked")
            ElementBinder<T> binder = (ElementBinder<T>) byType.get(argument.typeHashCode());
            if (binder != null) {
                return Optional.of(binder);
            } else {
                return Optional.of(DefaultTopicBinder.instance());
            }
        }
    }
}
