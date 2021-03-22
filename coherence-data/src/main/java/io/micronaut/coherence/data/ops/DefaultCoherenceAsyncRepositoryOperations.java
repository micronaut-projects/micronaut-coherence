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

import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.coherence.dslquery.Statement;
import com.tangosol.coherence.dslquery.StatementResult;
import com.tangosol.net.AsyncNamedMap;
import com.tangosol.util.Processors;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.coherence.data.annotation.AsyncPersistEventSource;
import io.micronaut.coherence.data.annotation.AsyncRemoveEventSource;
import io.micronaut.coherence.data.annotation.AsyncUpdateEventSource;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.data.exceptions.EmptyResultException;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.UpdateOperation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
/**
 * Concrete {@link CoherenceAsyncRepositoryOperations} implementation using
 * a {@code Coherence} {@link AsyncNamedMap}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Prototype
class DefaultCoherenceAsyncRepositoryOperations
        implements CoherenceAsyncRepositoryOperations {

    /**
     * Executor isn't needed, but the API requires a non-{@code null} return.
     */
    private static final Executor SAME_THREAD_EXECUTOR = Runnable::run;

    /**
     * The associated {@link DefaultCoherenceRepositoryOperations}.
     */
    private final DefaultCoherenceRepositoryOperations repositoryOperations;

    /**
     * The {@link AsyncNamedMap}.
     */
    private AsyncNamedMap asyncNamedMap;

    // ----- constructors -----------------------------------------------

    /**
     * Constructs a new {@link DefaultCoherenceAsyncRepositoryOperations}.
     *
     * @param repositoryOperations the {@link DefaultCoherenceRepositoryOperations}
     */
    DefaultCoherenceAsyncRepositoryOperations(@Parameter final DefaultCoherenceRepositoryOperations repositoryOperations) {
        this.repositoryOperations = repositoryOperations;
    }

    // ----- CoherenceAsyncRepositoryOperations -------------------------

    @Override
    public <ID, T> AsyncNamedMap<ID, T> getAsyncNamedMap() {
        return ensureAsyncMap();
    }

    @Override
    public <ID, T> ID getId(final T entity) {
        return repositoryOperations.getId(entity);
    }

    // ----- AsyncRepositoryOperations ----------------------------------

    @NonNull
    @Override
    public Executor getExecutor() {
        return SAME_THREAD_EXECUTOR;
    }

    @NonNull
    @Override
    public <T> CompletionStage<T> findOne(@NonNull final Class<T> type, @NonNull final Serializable id) {
        return findOptional(type, id).thenApply(t -> {
            if (t == null) {
                throw new EmptyResultException();
            }
            return t;
        });
    }

    @NonNull
    @Override
    public <T, R> CompletionStage<R> findOne(@NonNull final PreparedQuery<T, R> preparedQuery) {
        return findOptional(preparedQuery).thenApply(r -> {
            if (r == null) {
                throw new EmptyResultException();
            }
            return r;
        });
    }

    @Override
    public <T> CompletionStage<Boolean> exists(@NonNull final PreparedQuery<T, Boolean> preparedQuery) {
        return executeAsync(preparedQuery)
                .thenApply(o -> !((Map) o).isEmpty());
    }

    @NonNull
    @Override
    public <T> CompletionStage<T> findOptional(@NonNull final Class<T> type, @NonNull final Serializable id) {
        return (CompletionStage<T>) getAsyncNamedMap().get(id);
    }

    @NonNull
    @Override
    public <T, R> CompletionStage<R> findOptional(@NonNull final PreparedQuery<T, R> preparedQuery) {
        CompletionStage stage = executeAsync(preparedQuery);

        return stage.thenApply(o -> {
            if (o == null) {
                return null;
            }
            if (o instanceof Map) {
                Map m = (Map) o;
                if (m.isEmpty()) {
                    return null;
                }
                return m.values().stream().findFirst().get();
            }
            return o;
        });
    }

    @NonNull
    @Override
    public <T> CompletionStage<Iterable<T>> findAll(final PagedQuery<T> pagedQuery) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    @NonNull
    @Override
    public <T> CompletionStage<Long> count(final PagedQuery<T> pagedQuery) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    @NonNull
    @Override
    public <T, R> CompletionStage<Iterable<R>> findAll(@NonNull final PreparedQuery<T, R> preparedQuery) {
        CompletionStage stage = executeAsync(preparedQuery);
        return stage.thenApply(o -> {
            if (o instanceof Map) {
                Map m = (Map) o;
                return m.values();
            } else if (o instanceof Number) {
                return Collections.singletonList(((Number) o).longValue());
            } else if (o instanceof Iterable) {
                return o;
            } else {
                throw new IllegalStateException("Unhandled type: " + o.getClass().getName());
            }
        });
    }

    @NonNull
    @Override
    @AsyncPersistEventSource
    public <T> CompletionStage<T> persist(@NonNull final InsertOperation<T> operation) {
        T entity = operation.getEntity();
        return getAsyncNamedMap().put(getId(entity), entity).thenApply(unused -> entity);
    }

    @NonNull
    @Override
    @AsyncUpdateEventSource
    public <T> CompletionStage<T> update(@NonNull final UpdateOperation<T> operation) {
        T entity = operation.getEntity();
        return getAsyncNamedMap().put(getId(entity), entity).thenApply(unused -> entity);
    }

    @NonNull
    @Override
    @AsyncRemoveEventSource
    public <T> CompletionStage<Number> delete(@NonNull final DeleteOperation<T> operation) {
        T entity = operation.getEntity();
        return getAsyncNamedMap().remove(getId(entity), entity).thenApply(aBoolean -> aBoolean ? 1 : 0);
    }

    @NonNull
    @Override
    public <T> CompletionStage<Iterable<T>> persistAll(@NonNull final InsertBatchOperation<T> operation) {
        Map<?, T> entitiesToSave = new HashMap<>();
        operation.forEach(t -> entitiesToSave.put(getId(t), t));
        return getAsyncNamedMap().putAll(entitiesToSave).thenApply(unused -> entitiesToSave.values());
    }

    @NonNull
    @Override
    public CompletionStage<Number> executeUpdate(@NonNull final PreparedQuery<?, Number> preparedQuery) {
        CompletionStage stage = executeAsync(preparedQuery);
        return stage.thenApply(o -> {
            if (o instanceof Map) {
                return ((Map) o).size();
            } else if (o instanceof Set) {
                return ((Set) o).size();
            } else {
                throw new IllegalStateException("unhandled return type");
            }
        });
    }

    @NonNull
    @Override
    public <T> CompletionStage<Number> deleteAll(@NonNull final DeleteBatchOperation<T> operation) {
        Map<?, T> entitiesToDelete = new LinkedHashMap<>();
        operation.forEach(t -> entitiesToDelete.put(getId(t), t));
        return getAsyncNamedMap().invokeAll(entitiesToDelete.keySet(), Processors.remove()).thenApply(Map::size);
    }

    @NonNull
    @Override
    public <R> CompletionStage<Page<R>> findPage(@NonNull final PagedQuery<R> pagedQuery) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    // ----- helper methods ---------------------------------------------

    private AsyncNamedMap ensureAsyncMap() {
        if (asyncNamedMap == null) {
            asyncNamedMap = repositoryOperations.ensureNamedMap().async();
        }
        return asyncNamedMap;
    }

    private CompletionStage<?> executeAsync(PreparedQuery preparedQuery) {
        ExecutionContext ctx = repositoryOperations.ensureExecutionContext();
        Statement statement = repositoryOperations.createStatement(ctx, preparedQuery);
        return statement.executeAsync(ctx).thenApply(StatementResult::getResult);
    }
}
