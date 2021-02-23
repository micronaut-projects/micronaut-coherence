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

import io.micronaut.coherence.annotation.Synchronous;
import io.micronaut.inject.ExecutableMethod;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Coherence event observer implementation that wraps an {@link io.micronaut.inject.ExecutableMethod}.
 *
 * @param <E> the event type
 * @param <T> the executable method's declaring type
 * @param <R> the executable method's return type
 * @author Jonathan Knight
 * @since 1.0
 */
abstract class BaseExecutableMethodObserver<E, T, R> {

    protected final Supplier<T> beanSupplier;

    protected final ExecutableMethod<T, R> method;

    protected final EventArgumentBinderRegistry<E> binderRegistry;

    /**
     * Create a {@link ExecutableMethodEventObserver}.
     *
     * @param supplier  a {@link Supplier} to lazily provide the Micronaut bean that has the executable method
     * @param method    the method to execute when events are received
     * @param registry  the {@link EventArgumentBinderRegistry} to use to bind arguments to the method
     */
    protected BaseExecutableMethodObserver(Supplier<T> supplier, ExecutableMethod<T, R> method, EventArgumentBinderRegistry<E> registry) {
        this.beanSupplier = supplier;
        this.method = method;
        this.binderRegistry = registry;
    }

    public String getId() {
        return method.toString();
    }

    public Set<Annotation> getObservedQualifiers() {
        return Stream.concat(Arrays.stream(method.getTargetMethod().getParameterAnnotations()[0]),
                Arrays.stream(method.getTargetMethod().getAnnotations()))
                .collect(Collectors.toSet());
    }

    public boolean isAsync() {
        return !method.hasAnnotation(Synchronous.class);
    }
}
