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
package io.micronaut.coherence.data.ops;

import com.tangosol.net.AsyncNamedMap;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;

/**
 * Exposes {@code async repository} operations specific to Coherence.
 */
public interface CoherenceAsyncRepositoryOperations extends AsyncRepositoryOperations {
    /**
     * Obtain the {@link AsyncNamedMap} associated with this {@code repository}.
     *
     * @param <ID> the type of the entity id
     * @param <T> the entity type
     *
     * @return the {@link AsyncNamedMap} associated this this {@code repository}
     */
    <ID, T> AsyncNamedMap<ID, T> getAsyncNamedMap();


    /**
     * Return the id associated with the specified entity.
     *
     * @param entity the entity to interrogate
     *
     * @param <ID> the type of the entity id
     * @param <T> the entity type
     *
     * @return the id associated with the specified entity
     */
    <ID, T> ID getId(T entity);
}
