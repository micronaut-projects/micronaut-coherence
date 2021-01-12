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
import io.micronaut.core.convert.ArgumentConversionContext;

import java.util.Optional;

/**
 * An {@link io.micronaut.core.bind.ArgumentBinder} for Coherence events.
 *
 * @param <T> the argument type
 * @param <E> the type of event
 * @author Jonathan Knight
 * @since 1.0
 */
class EventArgumentBinder<T, E> implements ArgumentBinder<T, E> {
    @Override
    @SuppressWarnings("unchecked")
    public BindingResult<T> bind(ArgumentConversionContext<T> context, E source) {
        return () -> Optional.of((T) source);
    }
}
