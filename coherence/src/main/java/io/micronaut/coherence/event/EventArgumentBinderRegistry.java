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

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.type.Argument;

import java.util.Optional;

/**
 * An {@link io.micronaut.core.bind.ArgumentBinderRegistry} for Coherence events.
 *
 * @param <E> the type of event
 * @author Jonathan Knight
 * @since 1.0
 */
class EventArgumentBinderRegistry<E> implements ArgumentBinderRegistry<E> {
    @Override
    public <T> Optional<ArgumentBinder<T, E>> findArgumentBinder(Argument<T> argument, E source) {
        if (argument.getType().isAssignableFrom(source.getClass())) {
            return Optional.of(new EventArgumentBinder<>());
        } else {
            return Optional.empty();
        }
    }
}
