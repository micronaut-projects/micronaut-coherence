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
package io.micronaut.coherence.data;

import com.oracle.coherence.repository.AbstractAsyncRepository;
import com.tangosol.net.AsyncNamedMap;
import io.micronaut.coherence.data.annotation.AsyncPersistEventSource;
import io.micronaut.coherence.data.annotation.AsyncRemoveEventSource;
import io.micronaut.coherence.data.interceptors.GetAsyncMapInterceptor;
import io.micronaut.coherence.data.interceptors.GetEntityTypeInterceptor;
import io.micronaut.coherence.data.interceptors.GetIdInterceptor;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.repository.GenericRepository;

import java.util.concurrent.CompletableFuture;

/**
 * While it's possible to annotate a simple {@link io.micronaut.data.repository.async.AsyncCrudRepository} with
 * {@link io.micronaut.coherence.data.annotation.CoherenceRepository} and use Coherence as a backend cache.  However,
 * to take full advantage of the feature set Coherence has to offer, it is recommended extending this class.  This
 * will give you all the features as documented in {@link AbstractAsyncRepository}.
 * <p>
 * Any class extending this class <em>must</em> be declared abstract and <em>cannot</em> implement any of
 * the Micronaut repository types due to overlap in methods between those interface methods and those provided
 * by {@link AbstractAsyncRepository}.
 *
 * @param <ID> the ID type
 * @param <T> the entity type
 */
public abstract class AbstractCoherenceAsyncRepository<T, ID>
        extends AbstractAsyncRepository<ID, T>
        implements GenericRepository<T, ID> {

    // ----- AbstractAsyncRepository ----------------------------------------

    @Override
    protected AsyncNamedMap<ID, T> getMap() {
        return getMapInternal();
    }

    @Override
    protected ID getId(final T t) {
        return getIdInternal(t);
    }

    @Override
    protected Class<? extends T> getEntityType() {
        return getEntityTypeInternal(null);
    }

    @AsyncPersistEventSource
    @Override
    public CompletableFuture<T> save(final T entity) {
        return super.save(entity);
    }

    @AsyncRemoveEventSource
    @Override
    public CompletableFuture<Boolean> remove(final T entity) {
        return super.remove(entity);
    }

    // ----- Helpers --------------------------------------------------------

    /**
     * This is in place to prevent Micronaut from scanning further up the inheritance tree and trying
     * to map {@link AbstractAsyncRepository#getId(Object)} and failing.
     * <p>
     * Called only by {@link #getId(Object)}.
     *
     * @param entity the entity
     *
     * @return the ID of the provided entity
     */
    @DataMethod(interceptor = GetIdInterceptor.class)
    protected abstract ID getIdInternal(T entity);

    /**
     * This is in place to prevent Micronaut from scanning further up the inheritance tree and trying
     * to map {@link AbstractAsyncRepository#getMap()} and failing.
     * <p>
     * Called only by {@link #getId(Object)}.
     *
     * @return the {@link AsyncNamedMap} for this {@code repository}
     */
    @DataMethod(interceptor = GetAsyncMapInterceptor.class)
    protected abstract AsyncNamedMap<ID, T> getMapInternal();

    /**
     * This is in place to prevent Micronaut from scanning further up the inheritance tree and trying
     * to map {@link AbstractAsyncRepository#getEntityType()} and failing.
     * <p>
     * Note: the value passed to this may always be {@code null}.  We don't care about the value
     * as due to how the Coherence API is defined, there can never be a value.  However, we can rely
     * on compile type information generated by Micronaut for this intercepted method and use that metadata
     * to return the entity type.
     * <p>
     * Called only by {@link #getEntityType()}.
     *
     * @param entity the entity
     *
     * @return the {@link Class} representing the entity type handled by this {@code repository}.
     */
    @SuppressWarnings("SameParameterValue")
    @DataMethod(interceptor = GetEntityTypeInterceptor.class)
    protected abstract Class<? extends T> getEntityTypeInternal(T entity);
}
