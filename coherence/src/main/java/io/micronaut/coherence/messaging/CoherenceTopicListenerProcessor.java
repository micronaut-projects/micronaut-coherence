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
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.ExtractorBinding;
import io.micronaut.coherence.annotation.FilterBinding;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.SubscriberGroup;

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
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.Utils;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ElementArgumentBinderRegistry<?> registry;

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
     * @param context             the Micronaut bean context
     * @param filterFactories     the filter factory to use to produce {@link com.tangosol.util.Filter Filters}
     * @param extractorFactories  the extractor factory to use to produce
     *                            {@link com.tangosol.util.ValueExtractor ValueExtractors}
     */
    @Inject
    public CoherenceTopicListenerProcessor(@Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService,
                                           ApplicationContext context,
                                           FilterFactories filterFactories,
                                           ExtractorFactories extractorFactories) {
        this.scheduler = Schedulers.from(executorService);
        this.context = context;
        this.filterFactories = filterFactories;
        this.extractorFactories = extractorFactories;
        this.registry = new ElementArgumentBinderRegistry<>();
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
                LOG.info("Skipping @CoherenceTopicListener annotated method subscription " + method
                         + " Session " + sessionName + " does not exist on Coherence instance " + coherence.getName());
                return;
            }

            Session session = coherence.getSession(sessionName);

            PublisherHolder[] sendToPublishers;
            String[] sendToTopics = Utils.getSendToTopicNames(method);
            if (sendToTopics.length > 0) {
                if (method.getReturnType().isVoid()) {
                    LOG.info("Skipping @SendTo annotations for @CoherenceTopicListener annotated method " + method
                             + " - method return type is void");
                    sendToPublishers = new PublisherHolder[0];
                } else {
                    sendToPublishers = new PublisherHolder[sendToTopics.length];
                    for (int i = 0; i < sendToTopics.length; i++) {
                        NamedTopic<?> topic = session.getTopic(sendToTopics[i]);
                        sendToPublishers[i] = new PublisherHolder(sendToTopics[i], topic.createPublisher());
                    }
                }
            } else {
                sendToPublishers = new PublisherHolder[0];
            }

            method.stringValue(SubscriberGroup.class).ifPresent(name -> options.add(Subscriber.Name.of(name)));

            List<String> filterBindings = method.getAnnotationNamesByStereotype(FilterBinding.class);
            if (filterBindings.size() > 0) {
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
            if (extractorBindings.size() > 0) {
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
System.err.println("****** Starting subscriber " + topicName);

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
        private final PublisherHolder[] publishers;

        /**
         * The bean declaring the {@link ExecutableMethod}.
         */
        private final T bean;

        /**
         * The {@link ExecutableMethod} to forward topic elements to.
         */
        private final ExecutableMethod<T, R> method;

        /**
         * The {@link ElementArgumentBinderRegistry} to use to bind method arguments.
         */
        private final ElementArgumentBinderRegistry<E> registry;

        /**
         * The scheduler service.
         */
        private final Scheduler scheduler;

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
        TopicSubscriber(String topicName, Subscriber<E> subscriber, PublisherHolder[] publishers, T bean,
                        ExecutableMethod<T, R> method, ElementArgumentBinderRegistry<E> registry, Scheduler scheduler) {
            this.topicName = topicName;
            this.subscriber = subscriber;
            this.publishers = publishers;
            this.bean = bean;
            this.method = method;
            this.registry = registry;
            this.scheduler = scheduler;
        }

        @Override
        public void close() {
            try {
                subscriber.close();
            } catch (Throwable t) {
                LOG.error("Error closing subscriber for topic " + topicName, t);
            }
        }

        /**
         * <p>Request the next message from the {@link com.tangosol.net.topic.Subscriber}.</p>
         * <p>If requesting the next message throws an exception the subscription will
         * end and the {@link com.tangosol.net.topic.Subscriber} will be closed.</p>
         */
        private void nextMessage() {
            subscriber.receive().handle(this::handleMessage)
                .handle((v, err) -> {
                    if (err != null) {
                        LOG.error("Error requesting message from topic " + topicName
                                + " for method " + method + " - subscriber will be closed", err);
                        subscriber.close();
                    }
                    return VOID;
                });
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
        private Void handleMessage(Subscriber.Element<E> element, Throwable throwable) {
            if (throwable != null) {
                if (!(throwable instanceof CancellationException)) {
                    LOG.error("Error receiving message from topic " + topicName
                            + " for method " + method + " - subscriber will be closed", throwable);
                    subscriber.close();
                }
                return VOID;
            }

            try {
                E value = element.getValue();
                Map<Argument<?>, Object> mapBindings = Collections.singletonMap(Argument.of(value.getClass()), value);
                ExecutableBinder<E> batchBinder = new DefaultExecutableBinder<>(mapBindings);
                BoundExecutable<T, R> boundExecutable = batchBinder.bind(method, registry, value);

                Object result = boundExecutable.invoke(bean);
                handleResult(result);
            } catch (Throwable t) {
                LOG.error("Error processing message from topic " + topicName, t);
            }
            nextMessage();
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
                Flowable<?> resultFlowable;
                boolean isBlocking;
                if (Publishers.isConvertibleToPublisher(result)) {
                    resultFlowable = Publishers.convertPublisher(result, Flowable.class);
                    isBlocking = method.hasAnnotation(Blocking.class);
                } else {
                    resultFlowable = Flowable.just(result);
                    isBlocking = true;
                }
                handleResultFlowable(method, resultFlowable, isBlocking);
            }
        }

        /**
         * Handle a listener method result that is a reactive flowable.
         *
         * @param method          the listener method
         * @param resultFlowable  the flowable result
         * @param isBlocking      {@code true} if the method is blocking
         */
        private void handleResultFlowable(ExecutableMethod<?, ?> method, Flowable<?> resultFlowable, boolean isBlocking) {
            Flowable<?> recordMetadataProducer = resultFlowable.subscribeOn(scheduler)
                    .flatMap((Function<Object, org.reactivestreams.Publisher<?>>) o -> {
                        if (ArrayUtils.isNotEmpty(publishers)) {
                            return Flowable.create(emitter -> {
                                for (PublisherHolder publisher : publishers) {
                                    publisher.send(o).handle((ignored, exception) -> {
                                        if (exception != null) {
                                            emitter.onError(exception);
                                        }
                                        return VOID;
                                    });
                                }
                                emitter.onComplete();
                            }, BackpressureStrategy.ERROR);
                        }
                        return Flowable.empty();
                    }).onErrorResumeNext(throwable -> {
                        LOG.error("Error processing result from method " + method, throwable);
                        return Flowable.empty();
                    });

            if (isBlocking) {
                recordMetadataProducer.blockingSubscribe(recordMetadata -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Method [{}] produced record metadata: {}", method, recordMetadata);
                    }
                });
            } else {
                //noinspection ResultOfMethodCallIgnored
                recordMetadataProducer.subscribe(recordMetadata -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Method [{}] produced record metadata: {}", method, recordMetadata);
                    }
                });
            }
        }
    }

    /**
     * An {@link io.micronaut.core.bind.ArgumentBinderRegistry} for Coherence topic elements.
     *
     * @param <E> the type of element
     */
    static class ElementArgumentBinderRegistry<E>
            implements ArgumentBinderRegistry<E> {
        @Override
        public <T> Optional<ArgumentBinder<T, E>> findArgumentBinder(Argument<T> argument, E source) {
            if (argument.getType().isAssignableFrom(source.getClass())) {
                return Optional.of(new ElementArgumentBinder<>());
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * An {@link io.micronaut.core.bind.ArgumentBinder} for Coherence topic elements.
     *
     * @param <T> the argument type
     * @param <E> the type of element
     */
    static class ElementArgumentBinder<T, E> implements ArgumentBinder<T, E> {
        @Override
        @SuppressWarnings("unchecked")
        public BindingResult<T> bind(ArgumentConversionContext<T> context, E source) {
            return () -> Optional.of((T) source);
        }
    }

    /**
     * A simple holder for a publisher and topic name.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static class PublisherHolder implements AutoCloseable {
        private final String topicName;
        private final Publisher publisher;

        public PublisherHolder(String topicName, Publisher<?> publisher) {
            this.topicName = topicName;
            this.publisher = publisher;
        }

        public String getTopicName() {
            return topicName;
        }

        public Publisher<?> getPublisher() {
            return publisher;
        }

        @Override
        public void close() {
            publisher.close();
        }

        CompletableFuture<Void> send(Object message) {
            return publisher.send(message);
        }
    }
}
