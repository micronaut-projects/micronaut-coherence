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
package io.micronaut.coherence.data.interceptors;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.data.model.runtime.EntityInstanceOperation;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Base class for async operation interceptors.
 */
public abstract class AbstractAsyncEventSourceInterceptor extends AbstractEventSourceInterceptor {

    /**
     * Constructs a new AbstractAsyncEventSourceInterceptor.
     *
     * @param registry the {@link RuntimeEntityRegistry}
     */
    public AbstractAsyncEventSourceInterceptor(final RuntimeEntityRegistry registry) {
        super(registry);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object intercept(final MethodInvocationContext context) {
        Object[] parameters = context.getParameterValues();
        if (parameters != null && parameters.length == 1) {
            Object eventFor = parameters[0];
            if (eventFor instanceof EntityInstanceOperation) {
                eventFor = ((EntityInstanceOperation) eventFor).getEntity();
            }

            final Object eventLocal = eventFor;
            if (!trigger(getHandledPreEventType(), eventFor)) {
                switch (getEventGroup()) {
                    case REMOVE:
                        if (context.getTarget() instanceof AsyncRepositoryOperations) {
                            return CompletableFuture.completedFuture(0);
                        }
                        return CompletableFuture.<Void>completedFuture(null);
                    case UPDATE:
                    case PERSIST:
                    default:
                        return CompletableFuture.supplyAsync(() -> eventLocal);
                }
            }

            return ((CompletionStage) context.proceed()).thenApplyAsync(result -> {
                    trigger(getHandledPostEventType(),
                            eventLocal.getClass().isInstance(result)
                                    ? result
                                    : eventLocal);
                return result;
            });
        }

        return context.proceed();
    }
}
