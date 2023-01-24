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
package io.micronaut.coherence.messaging;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.micronaut.coherence.annotation.*;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import com.tangosol.util.Filter;

import com.tangosol.util.ValueExtractor;
import io.micronaut.coherence.ExtractorFactories;
import io.micronaut.coherence.FilterFactories;
import io.micronaut.coherence.messaging.binders.ElementArgumentBinderRegistry;
import io.micronaut.coherence.messaging.exceptions.CoherenceSubscriberException;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import io.micronaut.scheduling.TaskExecutors;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * <p>A processor for processing methods annotated with {@literal @}{@link CoherenceTopicListener}.</p>
 *
 * <p>The {@link #process(io.micronaut.inject.BeanDefinition, io.micronaut.inject.ExecutableMethod)}
 * method will be called for each {@literal @}{@link CoherenceTopicListener} annotated method so that
 * this processor can discover all of th listeners. This bean is also a {@link Coherence.LifecycleListener}
 * so that the actual topic subscription will be done after Coherence starts.</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
class CoherenceTopicListenerProcessor
        implements ExecutableMethodProcessor<CoherenceTopicListener>, Coherence.LifecycleListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(CoherenceTopicListenerProcessor.class);

    private static final Void VOID = null;

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext context;

    /**
     * The filter factory to use to produce {@link com.tangosol.util.Filter Filters}.
     */
    private final FilterFactories filterFactories;

    /**
     * The filter factory to use to produce {@link com.tangosol.util.ValueExtractor ValueExtractors}.
     */
    private final ExtractorFactories extractorFactories;

    /**
     * The discovered methods annotated with {@literal @}{@link CoherenceTopicListener}.
     */
    private final List<MethodHolder> methods = new ArrayList<>();

    /**
     * The list of subscribers created.
     */
    private final List<TopicSubscriber<?, ?, ?>> subscribers = new ArrayList<>();

    /**
     * The argument binder registry to use to bind method arguments.
     */
    private final ElementArgumentBinderRegistry registry;

    /**
     * The scheduler service.
     */
    private final Scheduler scheduler;

    /**
     * A flag indicating whether all of the discovered subscriber methods have been subscribed.
     */
    private volatile boolean subscribed;

    /**
     * Create a {@link CoherenceTopicListenerProcessor}.
     *
     * @param executorService     the executor service
     * @param registry            the binder registry
     * @param context             the Micronaut bean context
     * @param filterFactories     the filter factory to use to produce {@link com.tangosol.util.Filter Filters}
     * @param extractorFactories  the extractor factory to use to produce
     *                            {@link com.tangosol.util.ValueExtractor ValueExtractors}
     */
    @Inject
    public CoherenceTopicListenerProcessor(@Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService,
                                           ElementArgumentBinderRegistry registry,
                                           ApplicationContext context,
                                           FilterFactories filterFactories,
                                           ExtractorFactories extractorFactories) {
        this.scheduler = Schedulers.fromExecutor(executorService);
        this.context = context;
        this.filterFactories = filterFactories;
        this.extractorFactories = extractorFactories;
        this.registry = registry;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        methods.add(new MethodHolder(beanDefinition, method));
    }

    @Override
    public void onEvent(CoherenceLifecycleEvent event) {
        if (event.getType() == CoherenceLifecycleEvent.Type.STARTED) {
            Coherence coherence = event.getCoherence();
            createSubscribers(coherence);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        subscribers.forEach(TopicSubscriber::close);
        subscribers.clear();
    }

    /**
     * Return {@code true} if all subscriber methods have been subscribed.
     *
     * @return {@code true} if all subscriber methods have been subscribed
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void createSubscribers(Coherence coherence) {
        for (MethodHolder holder : methods) {
            List<Subscriber.Option> options = new ArrayList<>();
            ExecutableMethod<?, ?> method = holder.getMethod();

            String topicName = Utils.getFirstTopicName(method)
                    .orElse(method.getMethodName());

            String  sessionName = method.stringValue(SessionName.class).orElse(Coherence.DEFAULT_NAME);
            if (!coherence.hasSession(sessionName)) {
                LOG.info("Skipping @CoherenceTopicListener annotated method subscription {} Session {} does not exist on Coherence instance {}", method, sessionName, coherence.getName());
                return;
            }

            Session session = coherence.getSession(sessionName);

            Publisher[] sendToPublishers;
            String[] sendToTopics = Utils.getSendToTopicNames(method);
            if (sendToTopics.length > 0) {
                if (method.getReturnType().isVoid()) {
                    LOG.info("Skipping @SendTo annotations for @CoherenceTopicListener annotated method {} - method return type is void", method);
                    sendToPublishers = new Publisher[0];
                } else {
                    sendToPublishers = new Publisher[sendToTopics.length];
                    for (int i = 0; i < sendToTopics.length; i++) {
                        NamedTopic<?> topic = session.getTopic(sendToTopics[i]);
                        sendToPublishers[i] = topic.createPublisher();
                    }
                }
            } else {
                sendToPublishers = new Publisher[0];
            }

            method.stringValue(SubscriberGroup.class).ifPresent(name -> options.add(Subscriber.Name.of(name)));

            List<String> filterBindings = method.getAnnotationNamesByStereotype(FilterBinding.class);
            if (!filterBindings.isEmpty()) {
                Set<Annotation> annotations = filterBindings.stream()
                        .map(s -> method.getAnnotationType(s).orElse(null))
                        .filter(Objects::nonNull)
                        .map(method::synthesize)
                        .collect(Collectors.toSet());

                Filter filter = filterFactories.resolve(annotations);
                if (filter != null) {
                    options.add(Subscriber.Filtered.by(filter));
                }
            }

            List<String> extractorBindings = method.getAnnotationNamesByStereotype(ExtractorBinding.class);
            if (!extractorBindings.isEmpty()) {
                Set<Annotation> annotations = extractorBindings.stream()
                        .map(s -> method.getAnnotationType(s).orElse(null))
                        .filter(Objects::nonNull)
                        .map(method::synthesize)
                        .collect(Collectors.toSet());

                ValueExtractor extractor = extractorFactories.resolve(annotations);
                if (extractor != null) {
                    options.add(Subscriber.Convert.using(extractor));
                }
            }

            BeanDefinition<?> beanDefinition = holder.getBeanDefinition();
            Class<?> clsBeanType = beanDefinition.getBeanType();
            Object bean = context.getBean(clsBeanType);

            NamedTopic<?> topic = session.getTopic(topicName);

            Subscriber<?> subscriber = topic.createSubscriber(options.toArray(options.toArray(new Subscriber.Option[0])));
            TopicSubscriber<?, ?, ?> topicSubscriber = new TopicSubscriber(topicName, subscriber, sendToPublishers, bean, method, registry, scheduler);
            subscribers.add(topicSubscriber);
            topicSubscriber.nextMessage();
        }
        subscribed = true;
    }

    /**
     * A simple holder for discovered subscriber methods.
     */
    static class MethodHolder {
        private final BeanDefinition<?> beanDefinition;
        private final ExecutableMethod<?, ?> method;

        public MethodHolder(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
            this.beanDefinition = beanDefinition;
            this.method = method;
        }

        public BeanDefinition<?> getBeanDefinition() {
            return beanDefinition;
        }

        public ExecutableMethod<?, ?> getMethod() {
            return method;
        }
    }

    /**
     * A topic subscriber that wraps an {@link ExecutableMethod}.
     *
     * @param <E> the type of the topic elements
     * @param <T> the type of the bean declaring the {@link ExecutableMethod}
     * @param <R> the method return type of the {@link ExecutableMethod}
     */
    static class TopicSubscriber<E, T, R> implements AutoCloseable {
        /**
         * The name of the subscribed topic.
         */
        private final String topicName;

        /**
         * The actual topic {@link com.tangosol.net.topic.Subscriber}.
         */
        private final Subscriber<E> subscriber;

        /**
         * The optional topic {@link com.tangosol.net.topic.Publisher Publishers} to send
         * any method return value to.
         */
        private final Publisher<?>[] publishers;

        /**
         * The bean declaring the {@link ExecutableMethod}.
         */
        private final T bean;

        /**
         * The {@link ExecutableMethod} to forward topic elements to.
         */
        private final ExecutableMethod<?, ?> method;

        /**
         * The subscriber argument.
         */
        private final Optional<Argument<?>> subscriberArg;

        /**
         * The {@link ElementArgumentBinderRegistry} to use to bind method arguments.
         */
        private final ElementArgumentBinderRegistry registry;

        /**
         * The scheduler service.
         */
        private final Scheduler scheduler;

        /**
         * The commit strategy to use to commit received messages.
         */
        private final CommitStrategy commitStrategy;

        /**
         * Create a {@link TopicSubscriber}.
         *
         * @param topicName        the name of the subscribed topic.
         * @param subscriber       the actual topic {@link com.tangosol.net.topic.Subscriber}
         * @param publishers       the optional {@link Publisher Publishers} to send any method return type to
         * @param bean             the bean declaring the {@link ExecutableMethod}
         * @param method           the {@link ExecutableMethod} to forward topic elements to
         * @param registry         the {@link ElementArgumentBinderRegistry} to use to bind method arguments
         * @param scheduler        the scheduler service
         */
        @SuppressWarnings({"rawtypes"})
        TopicSubscriber(String topicName, Subscriber<E> subscriber, Publisher<?>[] publishers, T bean,
                        ExecutableMethod<T, R> method, ElementArgumentBinderRegistry registry, Scheduler scheduler) {
            this.topicName = topicName;
            this.subscriber = subscriber;
            this.publishers = publishers;
            this.bean = bean;
            this.method = method;
            this.registry = registry;
            this.scheduler = scheduler;
            Class<? extends Subscriber> cls = subscriber.getClass();
            this.subscriberArg = Arrays.stream(method.getArguments())
                    .filter(arg -> Subscriber.class.isAssignableFrom(arg.getType()) && arg.getType().isAssignableFrom(cls))
                    .findFirst();
            this.commitStrategy = method.getValue(CoherenceTopicListener.class, "commitStrategy", CommitStrategy.class)
                                        .orElse(CommitStrategy.SYNC);
        }

        @Override
        public void close() {
            try {
                subscriber.close();
            } catch (Exception e) {
                LOG.error("Error closing subscriber for topic {}", topicName, e);
            }
        }

        /**
         * <p>Request the next message from the {@link com.tangosol.net.topic.Subscriber}.</p>
         * <p>If requesting the next message throws an exception the subscription will
         * end and the {@link com.tangosol.net.topic.Subscriber} will be closed.</p>
         */
        private void nextMessage() {
            if (subscriber.isActive()) {
                subscriber.receive().handle(this::handleMessage)
                        .handle((v, err) -> {
                            if (err != null) {
                                LOG.error("Error requesting message from topic {} for method {} - subscriber will be closed", topicName, method, err);
                                subscriber.close();
                            }
                            return VOID;
                        });
            }
        }

        /**
         * <p>Handle the next async response from the subscriber.</p>
         * <p>After the {@link io.micronaut.inject.ExecutableMethod} handles the message
         * the next message will be requested from the subscriber.</p>
         * <p>If the response is an error the subscription will end and the
         * {@link com.tangosol.net.topic.Subscriber} will be closed.</p>
         * <p>If the call to the {@link io.micronaut.inject.ExecutableMethod} throws
         * an exception the subscription will end and the {@link com.tangosol.net.topic.Subscriber}
         * will be closed.</p>
         *
         * @param element    the {@link com.tangosol.net.topic.Subscriber.Element} received
         * @param throwable  any error from the subscriber
         *
         * @return always returns {@link java.lang.Void} (i.e. {@code null})
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        private Void handleMessage(Subscriber.Element<E> element, Throwable throwable) {
            SubscriberExceptionHandler.Action action = SubscriberExceptionHandler.Action.Continue;
            Throwable error = null;

            if (throwable == null) {
                try {
                    Map<Argument<?>, Object> mapBindings = new HashMap<>();
                    subscriberArg.ifPresent(arg -> mapBindings.put(arg, subscriber));

                    ExecutableBinder batchBinder = new DefaultExecutableBinder<>(mapBindings);
                    BoundExecutable boundExecutable = batchBinder.bind(method, registry, element);

                    Object result = boundExecutable.invoke(bean);
                    handleResult(result);
                } catch (Throwable thrown) {
                    error = thrown;
                }
            } else {
                error = throwable;
            }

            if (error == null) {
                // message processed successfully, do any commit action
                try {
                    if (commitStrategy != CommitStrategy.MANUAL) {
                        CompletableFuture<Subscriber.CommitResult> future = element.commitAsync();
                        if (commitStrategy == CommitStrategy.ASYNC) {
                            // async commit, so log any failure in a future handler
                            future.handle((result, commitError) -> {
                                if (commitError != null) {
                                    // With auto-commit strategies the developer has chosen to ignore commit failures, just log the error
                                    LOG.error("Error committing element channel={} position={}", element.getChannel(), element.getPosition(), commitError);
                                } else if (!result.isSuccess()) {
                                    // With auto-commit strategies the developer has chosen to ignore commit failures, just log the error
                                    LOG.error("Failed to commit element channel={} position={} status {}", element.getChannel(), element.getPosition(), result);
                                }
                                return VOID;
                            });
                        } else {
                            // sync commit so wait for it to complete
                            Subscriber.CommitResult result = future.join();
                            if (!result.isSuccess()) {
                                // With auto-commit strategies the developer has chosen to ignore commit failures, just log the error
                                LOG.error("Failed to commit element channel={} position={} status {}", element.getChannel(), element.getPosition(), result);
                            }
                        }
                    }
                } catch (Exception thrown) {
                    // With auto-commit strategies the developer has chosen to ignore commit failures, just log the error
                    LOG.error("Error committing element channel={} position={}", element.getChannel(), element.getPosition(), thrown);
                }
            } else if (error instanceof CancellationException) {
                // cancellation probably due to subscriber closing so we ignore the error
                action = SubscriberExceptionHandler.Action.Continue;
            } else {
                // an error occurred
                action = handleException(subscriber, method, element, error);
            }

            switch (action) {
                case Continue:
                    nextMessage();
                    break;
                case Stop:
                    subscriber.close();
                    break;
                default:
                    LOG.error("Unknown SubscriberExceptionHandler.Action {} closing subscriber", action);
                    subscriber.close();
            }

            return VOID;
        }

        /**
         * Handle the listener method result and if required forward to publishers.
         *
         * @param result the method result
         */
        private void handleResult(Object result) {
            if (result == null || publishers.length == 0) {
                return;
            }

            if (result.getClass().isArray()) {
                result = Arrays.asList((Object[]) result);
            }

            Class<?> type = result.getClass();
            boolean isAsyncReturnType = CompletionStage.class.isAssignableFrom(type);

            if (isAsyncReturnType) {
                ((CompletionStage<?>) result)
                        .handle((msg, err1) -> {
                            if (err1 == null) {
                                handleResult(msg);
                            } else {
                                LOG.error("Method " + method + " async result completed with an error", err1);
                            }
                            return VOID;
                        });
            } else {
                Flux<?> resultFlux;
                boolean isBlocking;
                if (Publishers.isConvertibleToPublisher(result)) {
                    resultFlux = Publishers.convertPublisher(result, Flux.class);
                    isBlocking = method.hasAnnotation(Blocking.class);
                } else {
                    resultFlux = Flux.just(result);
                    isBlocking = true;
                }
                handleResultFlux(method, resultFlux, isBlocking);
            }
        }

        /**
         * Handle a listener method result that is a reactive flux.
         *
         * @param method          the listener method
         * @param resultFlux      the flux result
         * @param isBlocking      {@code true} if the method is blocking
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        private void handleResultFlux(ExecutableMethod<?, ?> method, Flux<?> resultFlux, boolean isBlocking) {
            Flux<?> recordMetadataProducer = resultFlux.subscribeOn(scheduler)
                    .flatMap((Function<Object, org.reactivestreams.Publisher<?>>) o -> {
                        if (ArrayUtils.isNotEmpty(publishers)) {
                            return Flux.create(emitter -> {
                                for (Publisher publisher : publishers) {
                                    if (publisher.isActive()) {
                                        CompletableFuture<Publisher.Status> future = publisher.publish(o);
                                        future.handle((status, exception) -> {
                                            if (exception != null) {
                                                emitter.error(exception);
                                            } else {
                                                emitter.next(status);
                                            }
                                            return VOID;
                                        });
                                    }
                                }
                                emitter.complete();
                            }, FluxSink.OverflowStrategy.ERROR);
                        }
                        return Flux.empty();
                    }).onErrorResume(throwable -> {
                        LOG.error("Error processing result from method {}", method, throwable);
                        return Flux.empty();
                    });

            if (isBlocking) {
                recordMetadataProducer.toStream().forEach(recordMetadata -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Method [{}] produced record metadata: {}", method, recordMetadata);
                    }
                });
            } else {
                recordMetadataProducer.subscribe(recordMetadata -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Method [{}] produced record metadata: {}", method, recordMetadata);
                    }
                });
            }
        }

        private SubscriberExceptionHandler.Action handleException(Subscriber<?> subscriber, Object consumerBean, Subscriber.Element<?> element, Throwable e) {
            CoherenceSubscriberException exception = new CoherenceSubscriberException(
                    e,
                    consumerBean,
                    subscriber,
                    element
            );
            return handleException(consumerBean, exception);
        }

        private SubscriberExceptionHandler.Action handleException(Object consumerBean, CoherenceSubscriberException exception) {
            if (consumerBean instanceof SubscriberExceptionHandler) {
                return ((SubscriberExceptionHandler) consumerBean).handle(exception);
            } else {
                Subscriber.Element<?> element = exception.getElement().orElse(null);
                Throwable cause = exception.getCause();
                LOG.error("Closing subscriber due to error processing element [{}] for Coherence subscriber [{}] produced error: {}", element, consumerBean, cause.getMessage(), cause);
                return SubscriberExceptionHandler.Action.Stop;
            }
        }
    }
}
