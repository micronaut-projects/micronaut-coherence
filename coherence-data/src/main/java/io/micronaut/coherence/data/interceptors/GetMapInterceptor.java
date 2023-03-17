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

import com.tangosol.net.NamedMap;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.coherence.data.ops.CoherenceRepositoryOperations;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.intercept.AbstractQueryInterceptor;

/**
 * A {@link DataInterceptor} allowing {@link io.micronaut.coherence.data.AbstractCoherenceRepository} instances
 * to obtain the {@link NamedMap} associated with the {@link io.micronaut.coherence.data.AbstractCoherenceRepository}.
 *
 * @param <D> the declaring type
 * @param <T> the entity type
 * @param <ID> the ID type of the entity
 */
public final class GetMapInterceptor<ID, T, D>
        extends AbstractQueryInterceptor<D, NamedMap<ID, T>>
        implements DataInterceptor<D, NamedMap<ID, T>> {

    // ----- constructors -----------------------------------------------

    /**
     * Default constructor.
     *
     * @param operations the {@link RepositoryOperations}
     */
    public GetMapInterceptor(@NonNull RepositoryOperations operations) {
        super(operations);
    }

    // ----- DataInterceptor --------------------------------------------

    @Override
    public NamedMap<ID, T> intercept(final RepositoryMethodKey methodKey,
                                     final MethodInvocationContext<D, NamedMap<ID, T>> context) {
        return ((CoherenceRepositoryOperations) operations).getNamedMap();
    }
}
