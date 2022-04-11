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
package io.micronaut.coherence.data.ops;

import com.oracle.coherence.common.base.Logger;
import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.coherence.dslquery.Statement;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import com.tangosol.util.Processors;
import com.tangosol.util.QueryHelper;
import io.micronaut.coherence.data.annotation.PersistEventSource;
import io.micronaut.coherence.data.annotation.RemoveEventSource;
import io.micronaut.coherence.data.annotation.UpdateEventSource;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A {@link io.micronaut.data.operations.RepositoryOperations} implementation for Coherence.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@EachProperty(value = "coherence.data")
public class DefaultCoherenceRepositoryOperations implements CoherenceRepositoryOperations {

    /**
     * System property {@value}; used to enable logging of queries generated at compile time.
     */
    private static final String LOG_QUERIES_PROPERTY = "coherence.data.query.log";

    /**
     * Flag determining if queries are to be logged or not.
     *
     * @see #LOG_QUERIES_PROPERTY
     */
    private static final boolean LOG_QUERIES = Boolean.getBoolean(LOG_QUERIES_PROPERTY);

    /**
     * The name of the {@link NamedMap}.  This is pulled from application configuration.
     */
    private final String mapName;

    /**
     * The {@link BeanContext} used to lookup configured Sessions.
     */
    private final BeanContext beanContext;

    /**
     * Metadata pertaining to persisted entities.  This is primarily used to
     * obtain a reference to the entity ID for a given entity type.
     */
    private final ConcurrentMap<Class<?>, RuntimePersistentEntity> entities = new ConcurrentHashMap<>(5);

    /**
     * The name of the {@link Session}.  This is pulled from application configuration.
     */
    private String sessionName;

    /**
     * The {@link Session} obtained from application configuration.
     */
    private Session session;

    /**
     * The {@link NamedMap} obtained from the session.
     */
    private NamedMap namedMap;

    /**
     * The CohQL {@link ExecutionContext} to use when executing CohQL statements.
     */
    private ExecutionContext cohQLContext;

    /**
     * Associated {@link AsyncRepositoryOperations}.
     */
    private final CoherenceAsyncRepositoryOperations asyncOperations;

    /**
     * Associated {@link ApplicationContext}.
     */
    private final ApplicationContext applicationContext;

    // ----- constructors ---------------------------------------------------

    /**
     * Constructs a new operations instance.
     *
     * @param mapName the name of the {@link NamedMap} (from configuration)
     * @param applicationContext the {@link ApplicationContext}
     * @param beanContext the {@link BeanContext} used to look up a {@link Session} instance
     */
    public DefaultCoherenceRepositoryOperations(@Parameter String mapName,
                                                ApplicationContext applicationContext,
                                                BeanContext beanContext) {
        ArgumentUtils.requireNonNull("mapName", mapName);
        ArgumentUtils.requireNonNull("beanContext", beanContext);
        ArgumentUtils.requireNonNull("applicationContext", beanContext);
        this.mapName = mapName;
        this.beanContext = beanContext;
        this.asyncOperations = beanContext.createBean(DefaultCoherenceAsyncRepositoryOperations.class, this);
        this.applicationContext = applicationContext;
    }

    // ----- CoherenceRepositoryOperations interface ------------------------

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public <ID, T> NamedMap<ID, T> getNamedMap() {
        return ensureNamedMap();
    }

    @Override
    public <ID, T> ID getId(T entity) {
        RuntimePersistentEntity<T> rpe = ensureMeta(entity.getClass());

        RuntimePersistentProperty identityProp = rpe.getIdentity();
        assert identityProp != null;

        BeanProperty<?, T> beanProp = identityProp.getProperty();
        assert beanProp != null;

        return (ID) identityProp.getProperty().get(entity);
    }

    // ----- configuration --------------------------------------------------

    /**
     * Configure the name of the {@link Session}.  This is called during configuration.
     *
     * @param sessionName the name of the {@link Session}
     */
    @SuppressWarnings("unused")
    protected void setSession(final String sessionName) {
        ArgumentUtils.requireNonNull("sessionName", sessionName);
        this.sessionName = sessionName;
    }

    // ----- AsyncCapableRepository interface -------------------------------

    @NonNull
    @Override
    public AsyncRepositoryOperations async() {
        return asyncOperations;
    }

    // ----- RepositoryOperations interface ---------------------------------

    @Nullable
    @Override
    public <T> T findOne(@NonNull final Class<T> type, @NonNull final Serializable id) {
        return (T) getNamedMap().get(id);
    }

    @Nullable
    @Override
    public <T, R> R findOne(@NonNull final PreparedQuery<T, R> preparedQuery) {
        Object result = execute(preparedQuery);
        if (result instanceof Map) {
            Map m = (Map) result;
            if (m.isEmpty()) {
                return null;
            }
            return (R) m.values().stream().findFirst().orElse(null);
        } else if (result instanceof Number) {
            return (R) result;
        }
        throw new IllegalStateException("Unhandled type: " + result.getClass().getName());

    }

    @Override
    public <T> boolean exists(@NonNull final PreparedQuery<T, Boolean> preparedQuery) {
        Map m = (Map) execute(preparedQuery);
        return !m.isEmpty();
    }

    @NonNull
    @Override
    public <T> Iterable<T> findAll(@NonNull final PagedQuery<T> query) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    @Override
    public <T> long count(final PagedQuery<T> pagedQuery) {
        return getNamedMap().size();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T, R> Iterable<R> findAll(@NonNull final PreparedQuery<T, R> preparedQuery) {
        Object result = execute(preparedQuery);
        if (result instanceof Map) {
            Map m = (Map) result;
            return m.values();
        } else if (result instanceof Number) {
            return (Iterable<R>) Collections.singletonList(((Number) result).longValue());
        } else if (result instanceof Iterable) {
            return (Iterable<R>) result;
        }
        throw new IllegalStateException("Unhandled type: " + result.getClass().getName());
    }

    @NonNull
    @Override
    public <T, R> Stream<R> findStream(@NonNull final PreparedQuery<T, R> preparedQuery) {
        Map m = (Map) execute(preparedQuery);
        return (Stream<R>) m.values().stream();
    }

    @NonNull
    @Override
    public <T> Stream<T> findStream(@NonNull final PagedQuery<T> query) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    @Override
    public <R> Page<R> findPage(@NonNull final PagedQuery<R> query) {
        throw new UnsupportedOperationException("paging queries are not supported");
    }

    @NonNull
    @Override
    @PersistEventSource
    public <T> T persist(@NonNull final InsertOperation<T> operation) {
        T entity = operation.getEntity();
        getNamedMap().put(getId(entity), entity);
        return entity;
    }

    @NonNull
    @Override
    @UpdateEventSource
    public <T> T update(@NonNull final UpdateOperation<T> operation) {
        T entity = operation.getEntity();
        getNamedMap().put(getId(entity), entity);
        return entity;
    }

    @NonNull
    @Override
    public Optional<Number> executeUpdate(@NonNull final PreparedQuery<?, Number> preparedQuery) {
        Object result = execute(preparedQuery);
        if (result instanceof Map) {
            return Optional.of(((Map) result).size());
        } else if (result instanceof Set) {
            return Optional.of(((Set) result).size());
        } else {
            throw new IllegalStateException("unhandled return type");
        }
    }

    @Override
    public <T> Optional<Number> deleteAll(@NonNull final DeleteBatchOperation<T> operation) {
        Map<?, T> entitiesToDelete = new LinkedHashMap<>();
        operation.forEach(t -> entitiesToDelete.put(getId(t), t));
        Map result = getNamedMap().invokeAll(entitiesToDelete.keySet(), Processors.remove());
        return Optional.of(result.size());
    }

    @Override
    @RemoveEventSource
    public <T> int delete(@NonNull final DeleteOperation<T> operation) {
        T entity = operation.getEntity();
        boolean removed = getNamedMap().remove(getId(entity), entity);
        return removed ? 1 : 0;
    }

    @NonNull
    @Override
    public <T> Iterable<T> persistAll(@NonNull final InsertBatchOperation<T> operation) {
        Map<?, T> entitiesToSave = new HashMap<>();
        operation.forEach(t -> entitiesToSave.put(getId(t), t));
        getNamedMap().putAll(entitiesToSave);
        return entitiesToSave.values();
    }

    // ----- helper methods -------------------------------------------------

    /**
     * Creates a CohQL statement based on the provided {@link PreparedQuery}.
     *
     * @param context the {@link ExecutionContext}
     * @param preparedQuery the {@link PreparedQuery} to create a CohQL statement from
     *
     * @return a CohQL statement ready for execution
     */
    Statement createStatement(ExecutionContext context, PreparedQuery preparedQuery) {
        Map bindings = createBindingMap(preparedQuery);

        String query = replaceTarget(preparedQuery.getQuery(), preparedQuery.getRootEntity());
        Statement statement = QueryHelper.createStatement(query, context, bindings);

        logQuery(context, statement, query, bindings);

        return statement;
    }

    /**
     * Executes the provided {@link PreparedQuery}.
     *
     * @param preparedQuery the {@link PreparedQuery} to execute
     *
     * @return the result of query execution
     */
    private Object execute(PreparedQuery preparedQuery) {
        ExecutionContext ctx = ensureExecutionContext();
        Statement statement = createStatement(ctx, preparedQuery);
        return statement.execute(ctx).getResult();
    }

    /**
     * Used to obtain {@link RuntimePersistentEntity} information about an entity type.
     * This is primarily used to obtain the ID associated with any given entity.
     *
     * @param entityType the type of the entity
     *
     * @return the {@link RuntimePersistentEntity} for the given type
     */
    private RuntimePersistentEntity ensureMeta(Class entityType) {
        return entities.computeIfAbsent(entityType, RuntimePersistentEntity::new);
    }

    /**
     * Ensures a single {@link ExecutionContext} is available for CohQL execution.
     *
     * @return the {@link ExecutionContext}
     */
    ExecutionContext ensureExecutionContext() {
        ExecutionContext ctx = cohQLContext;
        if (ctx == null) {
            ctx = QueryHelper.createExecutionContext(ensureSession());
            ctx.setExtendedLanguage(false);
            cohQLContext = ctx;
        }
        return ctx;
    }

    /**
     * Return the {@link NamedMap} that should be used by this {@code repository}.
     *
     * @return return the {@link NamedMap} that should be used by this {@code repository}
     */
    NamedMap ensureNamedMap() {
        if (namedMap == null) {
            namedMap = ensureSession().getMap(mapName);
        }
        return namedMap;
    }

    /**
     * Return the {@link Session} that should be used by this {@code repository}.
     *
     * @return return the {@link Session} that should be used by this {@code repository}
     */
    protected Session ensureSession() {
        if (session == null) {
            session = beanContext.createBean(Session.class, sessionName == null ? Coherence.DEFAULT_NAME : sessionName);
        }
        return session;
    }

    /**
     * Replaces the generated target of the statement with the name {@link NamedMap} associated
     * with this {@link io.micronaut.data.operations.RepositoryOperations} instance.
     *
     * @param query the query as provided by Micronaut
     * @param entityClass the entity type
     *
     * @param <T> the entity type
     *
     * @return the modified query
     */
    protected <T> String replaceTarget(String query, Class<? extends T> entityClass) {
        // ugly hack to due to unexpected behavioral changes in micronaut-data 3.2.x; statements that result
        // in a boolean are now including TRUE in the select statement, which COHQL doesn't like.  Haven't
        // been able to determine a cleaner fix.
        String queryLocal = query.replace("SELECT TRUE", "SELECT ");

        // Another hack as it doesn't seem the CohQLQueryBuilder is called when generating delete statements.
//        queryLocal = queryLocal.replace("DELETE  ", "DELETE ");

        return queryLocal.replace(entityClass.getName(), getNamedMap().getName());
    }

    /**
     * Creates a map of binding parameters names and their values.
     *
     * @param preparedQuery the {@link PreparedQuery} to build the binding map from
     *
     * @return the parameters necessary to execute a CohQL statement
     */
    protected Map<String, Object> createBindingMap(PreparedQuery preparedQuery) {
        List<QueryParameterBinding> bindings = preparedQuery.getQueryBindings();
        int bindingsLen = bindings.size();
        String[] bindingNames = new String[bindingsLen];
        Integer[] bindingIndexes = new Integer[bindingsLen];

        for (int i = 0; i < bindingsLen; i++) {
            QueryParameterBinding binding = bindings.get(i);
            bindingNames[i] = binding.getName();
            bindingIndexes[i] = binding.getParameterIndex();
        }

        Object[] bindingValues = preparedQuery.getParameterArray();

        return IntStream.range(0, bindingNames.length).boxed()
                .collect(Collectors.toMap(i -> bindingNames[i],
                        i -> bindingValues[bindingIndexes[i]]));
    }

    /**
     * Logs the specified query and binding parameters.
     *
     * @param ctx the {@link ExecutionContext}
     * @param statement the {@link Statement}
     * @param query the query
     * @param bindingParams the binding parameters
     */
    protected void logQuery(ExecutionContext ctx, Statement statement, String query,
                            Map<String, Object> bindingParams) {
        if (LOG_QUERIES) {
            Logger.info(String.format("### Query: %s; parameters: %s", query, bindingParams));
            try (StringWriter sw = new StringWriter()) {
                PrintWriter f = new PrintWriter(sw, true);
                statement.showPlan(f);
                f.flush();
                Logger.info(String.format("### Query Plan: %s", sw.getBuffer().toString()));
            } catch (IOException e) {
                Logger.err("Error obtaining query plan", e);
            }
            Statement trace = QueryHelper.createStatement("TRACE " + query, ctx, bindingParams);
            Logger.info(trace.execute(ctx).getResult().toString());
        }
    }
}
