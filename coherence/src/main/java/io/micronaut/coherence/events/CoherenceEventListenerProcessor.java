/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.coherence.events;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.event.AnnotatedMapListener;
import com.oracle.coherence.event.AnnotatedMapListenerManager;
import com.oracle.coherence.event.Created;
import com.oracle.coherence.event.EventObserverSupport;
import com.oracle.coherence.event.Synchronous;

import com.tangosol.net.events.Event;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.internal.NamedEventInterceptor;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.MapEvent;
import com.tangosol.util.SafeLinkedList;

import io.micronaut.coherence.FilterFactories;
import io.micronaut.coherence.MapEventTransformerFactories;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

/**
 * A {@link ExecutableMethodProcessor} that processes methods annotated with
 * {@literal @}{@link CoherenceEventListener}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
@Context
public class CoherenceEventListenerProcessor
        extends AnnotatedMapListenerManager
        implements ExecutableMethodProcessor<CoherenceEventListener> {

    private final ApplicationContext ctx;

    private final EventArgumentBinderRegistry<?> binderRegistry;

    @SuppressWarnings("unchecked")
    private final List<NamedEventInterceptor<?>> interceptors = new SafeLinkedList();

    /**
     * Create the {@link CoherenceEventListenerProcessor} bean.
     *
     * @param beanContext        the {@link io.micronaut.context.BeanContext}
     * @param filterFactories    the factory to produce {@link com.tangosol.util.Filter} instances
     * @param transformerFactory the factory to produce {@link com.tangosol.util.MapEventTransformer} instances
     */
    @Inject
    public CoherenceEventListenerProcessor(ApplicationContext beanContext,
                                           FilterFactories filterFactories,
                                           MapEventTransformerFactories transformerFactory) {
        super(filterFactories, transformerFactory);
        ctx = beanContext;
        binderRegistry = new EventArgumentBinderRegistry<>();
    }

    /**
     * Returns the discovered interceptors.
     *
     * @return a list of discovered {@link NamedEventInterceptor} beans
     */
    public List<NamedEventInterceptor<?>> getInterceptors() {
        return interceptors;
    }


    /**
     * Process {@link io.micronaut.inject.ExecutableMethod} bean definitions for methods annotated with
     * {@link CoherenceEventListener}.
     * Each method will be turned into either an {@link com.tangosol.net.events.EventInterceptor} or a
     * {@link com.tangosol.util.MapListener}.
     *
     * @param beanDefinition The bean definition to process
     * @param method         The executable method
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        // must have a single parameter that is a Coherence event
        Argument<?>[] arguments = method.getArguments();
        Class<?> type = arguments.length == 1 ? arguments[0].getType() : null;
        if (type != null && (Event.class.isAssignableFrom(type) || MapEvent.class.isAssignableFrom(type))) {
            Class<?> clsBeanType = beanDefinition.getBeanType();
            Object oBean = ctx.getBean(clsBeanType);

            if (Event.class.isAssignableFrom(type)) {
                ExecutableMethodEventObserver observer = new ExecutableMethodEventObserver(oBean, method, binderRegistry);
                EventObserverSupport.EventHandler handler = EventObserverSupport
                        .createObserver((Class<? extends Event>) type, observer);
                NamedEventInterceptor interceptor = new NamedEventInterceptor(observer.getId(), handler);
                interceptors.add(interceptor);
            } else {
                // type is MapEvent
                ExecutableMethodMapListener listener = new ExecutableMethodMapListener(oBean, method, binderRegistry);
                AnnotatedMapListener mapListener = new AnnotatedMapListener(listener, listener.getObservedQualifiers());
                addMapListener(mapListener);
            }
        } else {
            throw new IllegalArgumentException("The @CoherenceEventListener annotated method "
                                                       + method
                    .getTargetMethod() + " must have a single Coherence Event or MapEvent argument.");
        }
    }


    /**
     * Listen for {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent.Type#CREATED Created}
     * {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent CacheLifecycleEvents}
     * and register relevant map listeners when caches are created.
     *
     * @param event the {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent}
     */
    @CoherenceEventListener
    void registerMapListeners(@Created CacheLifecycleEvent event) {
        registerListeners(event.getCacheName(), event.getScopeName(), event.getSessionName(), event.getServiceName());
    }

    /**
     * An {@link io.micronaut.core.bind.ArgumentBinderRegistry} for Coherence events.
     *
     * @param <E> the type of event
     */
    static class EventArgumentBinderRegistry<E>
            implements ArgumentBinderRegistry<E> {
        @Override
        public <T> Optional<ArgumentBinder<T, E>> findArgumentBinder(Argument<T> argument, E source) {
            if (argument.getType().isAssignableFrom(source.getClass())) {
                return Optional.of(new EventArgumentBinder<>());
            } else {
                return Optional.empty();
            }
        }
    }

    // ----- inner class: ExecutableMethodMapListener -----------------------

    /**
     * An {@link io.micronaut.core.bind.ArgumentBinder} for Coherence events.
     *
     * @param <T> the argument type
     * @param <E> the type of event
     */
    static class EventArgumentBinder<T, E> implements ArgumentBinder<T, E> {
        @Override
        @SuppressWarnings("unchecked")
        public BindingResult<T> bind(ArgumentConversionContext<T> context, E source) {
            return () -> Optional.of((T) source);
        }
    }

    // ----- data members ---------------------------------------------------

    /**
     * A Coherence event observer implementation that wraps an {@link io.micronaut.inject.ExecutableMethod}.
     *
     * @param <E> the event type
     * @param <T> the executable method's declaring type
     * @param <R> the executable method's return type
     */
    static class BaseExecutableMethodObserver<E, T, R> {
        protected final T f_oBean;
        protected final ExecutableMethod<T, R> f_method;
        protected final EventArgumentBinderRegistry<E> f_binderRegistry;

        public BaseExecutableMethodObserver(T oBean,
                                            ExecutableMethod<T, R> method,
                                            EventArgumentBinderRegistry<E> binderRegistry) {
            f_oBean = oBean;
            f_method = method;
            f_binderRegistry = binderRegistry;
        }

        public String getId() {
            return f_method.toString();
        }

        public Set<Annotation> getObservedQualifiers() {
            return Stream.concat(Arrays.stream(f_method.getTargetMethod().getParameterAnnotations()[0]),
                                 Arrays.stream(f_method.getTargetMethod().getAnnotations()))
                    .collect(Collectors.toSet());
        }

        public boolean isAsync() {
            return !f_method.hasAnnotation(Synchronous.class);
        }
    }

    /**
     * A Coherence event observer implementation that wraps an {@link io.micronaut.inject.ExecutableMethod}.
     *
     * @param <E> the event type
     * @param <T> the executable method's declaring type
     * @param <R> the executable method's return type
     */
    static class ExecutableMethodEventObserver<E extends Event<?>, T, R>
            extends BaseExecutableMethodObserver<E, T, R>
            implements EventObserverSupport.EventObserver<E> {
        public ExecutableMethodEventObserver(T oBean,
                                             ExecutableMethod<T, R> method,
                                             EventArgumentBinderRegistry<E> binderRegistry) {
            super(oBean, method, binderRegistry);
        }

        @Override
        public void notify(E event) {
            Map<Argument<?>, Object> mapBindings = Collections.singletonMap(Argument.of(LifecycleEvent.class), event);
            ExecutableBinder<E> batchBinder = new DefaultExecutableBinder<>(mapBindings);
            BoundExecutable<T, R> boundExecutable = batchBinder.bind(f_method, f_binderRegistry, event);
            boundExecutable.invoke(f_oBean);
        }
    }

    static class ExecutableMethodMapListener<K, V, T, R>
            extends BaseExecutableMethodObserver<MapEvent<K, V>, T, R>
            implements AnnotatedMapListener.MapEventObserver<K, V> {
        public ExecutableMethodMapListener(T oBean,
                                           ExecutableMethod<T, R> method,
                                           EventArgumentBinderRegistry<MapEvent<K, V>> binderRegistry) {
            super(oBean, method, binderRegistry);
        }

        @Override
        public void notify(MapEvent<K, V> event) {
            Map<Argument<?>, Object> mapBindings = Collections.singletonMap(Argument.of(LifecycleEvent.class), event);
            ExecutableBinder<MapEvent<K, V>> batchBinder = new DefaultExecutableBinder<>(mapBindings);
            BoundExecutable<T, R> boundExecutable = batchBinder.bind(f_method, f_binderRegistry, event);
            boundExecutable.invoke(f_oBean);
        }
    }
}
