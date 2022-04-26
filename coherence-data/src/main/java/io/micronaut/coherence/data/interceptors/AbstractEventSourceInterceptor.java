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

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.model.runtime.EntityInstanceOperation;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.event.DefaultEntityEventContext;

/**
 * Base class for sync event source interceptors.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractEventSourceInterceptor implements MethodInterceptor {
    /**
     * {@link RuntimeEntityRegistry} used for event notification.
     */
    protected RuntimeEntityRegistry registry;

    // ----- constructors ---------------------------------------------------

    /**
     * Constructs a new AbstractEventSourceInterceptor.
     *
     * @param registry the {@link RuntimeEntityRegistry}
     */
    protected AbstractEventSourceInterceptor(RuntimeEntityRegistry registry) {
        this.registry = registry;
    }

    // ----- MethodInterceptor methods --------------------------------------

    @Override
    public Object intercept(final MethodInvocationContext context) {
        Object[] parameters = context.getParameterValues();
        if (parameters.length == 1) {
            Object eventFor = parameters[0];

            if (eventFor instanceof EntityInstanceOperation) {
                eventFor = ((EntityInstanceOperation) eventFor).getEntity();
            }

            if (!trigger(getHandledPreEventType(), eventFor)) {
                switch (getEventGroup()) {
                    case REMOVE:
                        Object target = context.getTarget();
                        if (target instanceof RepositoryOperations) {
                            return 0;
                        }
                        return false;
                    case UPDATE:
                    case PERSIST:
                    default:
                        return eventFor;
                }
            }

            Object result = context.proceed();
            trigger(getHandledPostEventType(),
                    eventFor.getClass().isInstance(result)
                            ? result
                            : eventFor);
            return result;
        }

        return context.proceed();
    }

    /**
     * Return the {@code EventGroup} handled by this interceptor.
     *
     * @return the {@code EventGroup} handled by this interceptor
     */
    protected abstract EventGroup getEventGroup();

    /**
     * Return the {@code PRE} event type this interceptor wishes to emit.
     *
     * @return the {@code PRE} event type this interceptor wishes to emit
     */
    protected abstract EventType getHandledPreEventType();

    /**
     * Return the {@code POST} event type this interceptor wishes to emit.
     *
     * @return the {@code POST} event type this interceptor wishes to emit
     */
    protected abstract EventType getHandledPostEventType();

    /**
     * Trigger the specified event using the provided entity as the event value.
     *
     * @param eventType the {@link EventType}
     * @param entity the event value
     *
     * @return the result of any {@code PRE} events may be inspected to stop processing
     *         of an event.  Any values returned for {@code POST} events is meaningless.
     */
    @SuppressWarnings("unchecked")
    protected boolean trigger(EventType eventType, Object entity) {
        ArgumentUtils.requireNonNull("eventType", eventType);
        ArgumentUtils.requireNonNull("entity", eventType);

        RuntimePersistentEntity rpe = registry.getEntity(entity.getClass());

        switch (eventType) {
            case PRE_PERSIST:
                if (rpe.hasPrePersistEventListeners()) {
                    return registry.getEntityEventListener().prePersist(new DefaultEntityEventContext(rpe, entity));
                }
                break;
            case POST_PERSIST:
                if (rpe.hasPostPersistEventListeners()) {
                    registry.getEntityEventListener().postPersist(new DefaultEntityEventContext(rpe, entity));
                }
                break;
            case PRE_UPDATE:
                if (rpe.hasPreUpdateEventListeners()) {
                    return registry.getEntityEventListener().preUpdate(new DefaultEntityEventContext(rpe, entity));
                }
                break;
            case POST_UPDATE:
                if (rpe.hasPostUpdateEventListeners()) {
                    registry.getEntityEventListener().postUpdate(new DefaultEntityEventContext<>(rpe, entity));
                }
                break;
            case PRE_REMOVE:
                if (rpe.hasPreRemoveEventListeners()) {
                    return registry.getEntityEventListener().preRemove(new DefaultEntityEventContext<>(rpe, entity));
                }
                break;
            case POST_REMOVE:
            default:
                if (rpe.hasPostRemoveEventListeners()) {
                    registry.getEntityEventListener().postRemove(new DefaultEntityEventContext<>(rpe, entity));
                }
        }
        return true;
    }

    /**
     * Represents the logical operations for multiple event types (i.e., pre, post persist).
     */
    protected enum EventGroup {
        /**
         * Persist events.
         */
        PERSIST,

        /**
         * Update events.
         */
        UPDATE,

        /**
         * Remove events.
         */
        REMOVE
    }

    /**
     * Various entity event types used by Micronaut Data.
     */
    protected enum EventType {
        /**
         * @see io.micronaut.data.annotation.event.PrePersist
         */
        PRE_PERSIST,

        /**
         * @see io.micronaut.data.annotation.event.PostPersist
         */
        POST_PERSIST,

        /**
         * @see io.micronaut.data.annotation.event.PreUpdate
         */
        PRE_UPDATE,

        /**
         * @see io.micronaut.data.annotation.event.PostUpdate
         */
        POST_UPDATE,

        /**
         * @see io.micronaut.data.annotation.event.PreRemove
         */
        PRE_REMOVE,

        /**
         * @see io.micronaut.data.annotation.event.PostRemove
         */
        POST_REMOVE
    }
}
