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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.SessionName;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.topic.Publisher;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.coherence.annotation.CoherencePublisher;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.coherence.annotation.Topics;
import io.micronaut.coherence.annotation.Utils;
import io.micronaut.context.BeanContext;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.StringUtils;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.messaging.exceptions.MessagingClientException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link io.micronaut.coherence.annotation.CoherencePublisher} advice annotation.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
public class CoherencePublisherIntroductionAdvice implements MethodInterceptor<Object, Object>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(CoherencePublisherIntroductionAdvice.class);

    private final BeanContext beanContext;

    private final ConversionService<?> conversionService;

    private final Map<TopicKey, Publisher<Object>> publisherMap = new ConcurrentHashMap<>();

    /**
     * Creates the introduction advice for the given arguments.
     *
     * @param beanContext       the Micronaut bean context
     * @param conversionService the conversion service
     */
    CoherencePublisherIntroductionAdvice(BeanContext beanContext, ConversionService<?> conversionService) {
        this.beanContext = beanContext;
        this.conversionService = conversionService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(CoherencePublisher.class)) {
            // Make sure the CoherencePublisher annotation is present
            context.findAnnotation(CoherencePublisher.class)
                    .orElseThrow(() -> new IllegalStateException("No @CoherencePublisher annotation present on method: " + context));

            String topicName = Utils.getFirstTopicName(context).orElse(null);
            String sessionName = context.stringValue(SessionName.class).orElse(Coherence.DEFAULT_NAME);

            Duration maxBlock = context.getValue(CoherencePublisher.class, "maxBlock", Duration.class)
                    .orElse(null);

            Argument<?> bodyArgument = null;
            Argument<?>[] arguments = context.getArguments();
            Object[] parameterValues = context.getParameterValues();
            int valueIndex = -1;

            for (int i = 0; i < arguments.length; i++) {
                Argument<?> argument = arguments[i];
                if (argument.isAnnotationPresent(Body.class)) {
                    bodyArgument = argument;
                    valueIndex = i;
                } else if (argument.isAnnotationPresent(Topics.class) || argument.isAnnotationPresent(Topic.class)) {
                    Object o = parameterValues[i];
                    if (o != null) {
                        topicName = o.toString();
                    }
                }
            }

            if (StringUtils.isEmpty(topicName)) {
                throw new MessagingClientException("No topic specified for method: " + context);
            }

            if (bodyArgument == null) {
                for (int i = 0; i < arguments.length; i++) {
                    Argument<?> argument = arguments[i];
                    if (!argument.getAnnotationMetadata().hasStereotype(Bindable.class)) {
                        bodyArgument = argument;
                        valueIndex = i;
                        break;
                    }
                }
            }

            if (bodyArgument == null) {
                throw new MessagingClientException("No valid message body argument found for method: " + context);
            }

            Object value = parameterValues[valueIndex];

            ReturnType<Object> returnType = context.getReturnType();
            Class<?> javaReturnType = returnType.getType();
            Publisher<Object> publisher = getPublisher(topicName, sessionName);

            boolean isReactiveReturnType = Publishers.isConvertibleToPublisher(javaReturnType);
            boolean isReactiveValue = value != null && Publishers.isConvertibleToPublisher(value.getClass());

            if (isReactiveReturnType) {
                // return type is a reactive type
                Flowable<?> flowable = buildSendFlowable(context, publisher, Argument.OBJECT_ARGUMENT, maxBlock, value);
                return Publishers.convertPublisher(flowable, javaReturnType);
            } else {
                // return type is a future - must be future of Void
                Argument<?> returnArg = returnType.getFirstTypeVariable().orElse(Argument.of(Void.class));
                if (returnArg.getType() != Void.class) {
                    throw new MessagingClientException("Generic return type for method must be Void, i.e. CompletableFuture<Void> - " + context);
                }
                CompletableFuture<Void> completableFuture = new CompletableFuture<>();

                if (isReactiveValue) {
                    // return type is a future and value is reactive
                    Flowable<?> sendFlowable = buildSendFlowable(
                            context,
                            publisher,
                            returnArg,
                            maxBlock,
                            value
                    );

                    if (!Publishers.isSingle(value.getClass())) {
                        sendFlowable = sendFlowable.toList().toFlowable();
                    }

                    //noinspection ReactiveStreamsSubscriberImplementation
                    sendFlowable.subscribe(new Subscriber<Object>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1);
                        }

                        @Override
                        public void onNext(Object o) {
                        }

                        @Override
                        public void onError(Throwable t) {
                            completableFuture.completeExceptionally(wrapException(context, t));
                        }

                        @Override
                        public void onComplete() {
                            completableFuture.complete(null);
                        }
                    });
                } else {
                    // return type is a future and value is single message
                    publisher.send(value).handle((result, exception) -> {
                        if (exception != null) {
                            completableFuture.completeExceptionally(wrapException(context, exception));
                        } else {
                            completableFuture.complete(null);
                        }
                        return null;
                    });
                }

                try {
                    if (Future.class.isAssignableFrom(javaReturnType)) {
                        return completableFuture;
                    } else if (maxBlock != null) {
                        return completableFuture.get(maxBlock.toMillis(), TimeUnit.MILLISECONDS);
                    } else {
                        return completableFuture.get();
                    }
                } catch (Throwable t) {
                    throw wrapException(context, t);
                }
            }
        } else {
            // can't be implemented so proceed
            return context.proceed();
        }
    }

    @Override
    public void close() {
        for (Map.Entry<TopicKey, Publisher<Object>> entry : publisherMap.entrySet()) {
            Publisher<Object> publisher = entry.getValue();
            try {
                publisher.flush().get(1, TimeUnit.MINUTES);
            } catch (Throwable t) {
                LOG.error("Error flushing publisher", t);
            }

            try {
                publisher.close();
            } catch (Throwable t) {
                LOG.error("Error closing publisher", t);
            }
        }
    }

    @Nonnull
    private Publisher<Object> getPublisher(String topicName, String sessionName) {
        TopicKey key = new TopicKey(topicName, sessionName);

        return publisherMap.compute(key, (k, publisher) -> {
            if (publisher != null) {
                return publisher;
            }
            Session session = beanContext.createBean(Session.class, sessionName);
            return session.getTopic(topicName).createPublisher();
        });
    }

    private Flowable<Object> buildSendFlowable(
            MethodInvocationContext<Object, Object> context,
            Publisher<Object> publisher,
            Argument<?> returnType,
            Duration maxBlock,
            Object value) {

        Flowable<?> valueFlowable = Publishers.convertPublisher(value, Flowable.class);
        Class<?> javaReturnType = returnType.getType();

        if (Iterable.class.isAssignableFrom(javaReturnType)) {
            javaReturnType = returnType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT).getType();
        }

        Class<?> finalJavaReturnType = javaReturnType;
        Flowable<Object> sendFlowable = valueFlowable.flatMap(o -> {
            return Flowable.create(emitter -> publisher.send(o).handle((metadata, exception) -> {
                if (exception != null) {
                    emitter.onError(wrapException(context, exception));
                } else {
                    if (finalJavaReturnType.isInstance(o)) {
                        emitter.onNext(o);
                    } else {
                        conversionService.convert(metadata, finalJavaReturnType).ifPresent(emitter::onNext);
                    }
                    emitter.onComplete();
                }
                return null;
            }), BackpressureStrategy.BUFFER);
        });

        if (maxBlock != null) {
            sendFlowable = sendFlowable.timeout(maxBlock.toMillis(), TimeUnit.MILLISECONDS);
        }
        return sendFlowable;
    }

    private MessagingClientException wrapException(MethodInvocationContext<Object, Object> context, Throwable exception) {
        return new MessagingClientException(
                "Exception sending message for method [" + context + "]: " + exception.getMessage(), exception
        );
    }
}
