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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.AbstractQueryInterceptor;

/**
 * A {@link DataInterceptor} allowing {@link io.micronaut.coherence.data.AbstractCoherenceRepository} instances
 * to obtain the entity type managed by this {@code repository}.
 *
 * @param <T> the entity to obtain an id from
 * @param <Class> the entity type
 */
public final class GetEntityTypeInterceptor<T, Class>
        extends AbstractQueryInterceptor<T, Class>
        implements DataInterceptor<T, Class> {

    // ----- constructors -----------------------------------------------

    /**
     * Default constructor.
     *
     * @param operations the {@link RepositoryOperations}
     */
    public GetEntityTypeInterceptor(@NonNull RepositoryOperations operations) {
        super(operations);
    }

    // ----- DataInterceptor --------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public Class intercept(final RepositoryMethodKey methodKey, final MethodInvocationContext<T, Class> context) {
        return (Class) context.getArgumentTypes()[0];
    }
}
