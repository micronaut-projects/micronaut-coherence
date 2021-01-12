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

import com.tangosol.net.events.Event;
import com.tangosol.net.events.application.LifecycleEvent;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;

import java.util.Collections;
import java.util.Map;

/**
 * A Coherence event observer implementation that wraps an {@link io.micronaut.inject.ExecutableMethod}.
 *
 * @param <E> the event type
 * @param <T> the executable method's declaring type
 * @param <R> the executable method's return type
 * @author Jonathan Knight
 * @since 1.0
 */
class ExecutableMethodEventObserver<E extends Event<?>, T, R>
        extends BaseExecutableMethodObserver<E, T, R> {

    ExecutableMethodEventObserver(T bean, ExecutableMethod<T, R> method, EventArgumentBinderRegistry<E> binderRegistry) {
        super(bean, method, binderRegistry);
    }

    public void notify(E event) {
        Map<Argument<?>, Object> mapBindings = Collections.singletonMap(Argument.of(LifecycleEvent.class), event);
        ExecutableBinder<E> batchBinder = new DefaultExecutableBinder<>(mapBindings);
        BoundExecutable<T, R> boundExecutable = batchBinder.bind(method, binderRegistry, event);
        boundExecutable.invoke(bean);
    }
}
