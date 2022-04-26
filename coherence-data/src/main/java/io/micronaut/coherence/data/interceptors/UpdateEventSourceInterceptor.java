/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.coherence.data.interceptors;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.coherence.data.annotation.UpdateEventSource;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;

import javax.inject.Singleton;

/**
 * An {@link AbstractEventSourceInterceptor interceptor} for {@code update} events.
 */
@Singleton
@InterceptorBean(UpdateEventSource.class)
public class UpdateEventSourceInterceptor extends AbstractEventSourceInterceptor {

    // ----- constructors ---------------------------------------------------

    /**
     * Constructs a new {@code UpdateEventSourceInterceptor}.
     *
     * @param registry the {@link RuntimeEntityRegistry}
     */
    public UpdateEventSourceInterceptor(RuntimeEntityRegistry registry) {
       super(registry);
    }

    // ----- AbstractEventSourceInterceptor methods -------------------------

    @Override
    public EventGroup getEventGroup() {
        return EventGroup.UPDATE;
    }

    @Override
    public EventType getHandledPreEventType() {
        return EventType.PRE_UPDATE;
    }

    @Override
    public EventType getHandledPostEventType() {
        return EventType.POST_UPDATE;
    }
}
