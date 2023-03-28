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

import com.tangosol.util.MapEvent;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link BaseExecutableMethodObserver} that wraps a map listener executable method.
 *
 * @param <K> the map event key type
 * @param <V> the map event value type
 * @param <T> the executable method's declaring type
 * @param <R> the executable method's return type
 * @author Jonathan Knight
 * @since 1.0
 */
class ExecutableMethodMapListener<K, V, T, R>
        extends BaseExecutableMethodObserver<MapEvent<K, V>, T, R> {

    /**
     * Create a {@link ExecutableMethodEventObserver}.
     *
     * @param supplier  a {@link Supplier} to lazily provide the Micronaut bean that has the executable method
     * @param method    the method to execute when events are received
     * @param registry  the {@link EventArgumentBinderRegistry} to use to bind arguments to the method
     */
    ExecutableMethodMapListener(Supplier<T> supplier, ExecutableMethod<T, R> method, EventArgumentBinderRegistry<MapEvent<K, V>> registry) {
        super(supplier, method, registry);
    }

    /**
     * Forward the event to the underlying executable method.
     *
     * @param event the map event
     */
    void notify(MapEvent<K, V> event) {
        Map<Argument<?>, Object> mapBindings = Collections.singletonMap(Argument.of(MapEvent.class), event);
        ExecutableBinder<MapEvent<K, V>> batchBinder = new DefaultExecutableBinder<>(mapBindings);
        BoundExecutable<T, R> boundExecutable = batchBinder.bind(method, binderRegistry, event);
        boundExecutable.invoke(beanSupplier.get());
    }
}
