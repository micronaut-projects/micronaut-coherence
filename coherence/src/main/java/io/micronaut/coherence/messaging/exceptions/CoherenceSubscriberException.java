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
package io.micronaut.coherence.messaging.exceptions;

import com.tangosol.net.topic.Subscriber;
import io.micronaut.messaging.exceptions.MessageListenerException;

import java.util.Optional;

/**
 * @author Jonathan Knight
 * @since 1.0
 */
public class CoherenceSubscriberException extends MessageListenerException {

    private final Object listener;
    private final Subscriber<?> kafkaConsumer;
    private final Subscriber.Element<?> element;

    /**
     * Creates a new exception.
     *
     * @param message The message
     * @param listener The listener
     * @param kafkaConsumer The consumer
     * @param element The consumer record
     */
    public CoherenceSubscriberException(String message, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
        super(message);
        this.listener = listener;
        this.kafkaConsumer = kafkaConsumer;
        this.element = element;
    }

    /**
     * Creates a new exception.
     *
     * @param message The message
     * @param cause The cause
     * @param listener The listener
     * @param kafkaConsumer The consumer
     * @param element The consumer record
     */
    public CoherenceSubscriberException(String message, Throwable cause, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
        super(message, cause);
        this.listener = listener;
        this.kafkaConsumer = kafkaConsumer;
        this.element = element;
    }
    
    /**
     * Creates a new exception.
     *
     * @param cause The cause
     * @param listener The listener
     * @param kafkaConsumer The consumer
     * @param element The consumer record
     */
    public CoherenceSubscriberException(Throwable cause, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
        super(cause.getMessage(), cause);
        this.listener = listener;
        this.kafkaConsumer = kafkaConsumer;
        this.element = element;
    }

    /**
     * @return The bean that is the kafka listener
     */
    public Object getKafkaListener() {
        return listener;
    }

    /**
     * @return The consumer that produced the error
     */
    public Subscriber<?> getKafkaConsumer() {
        return kafkaConsumer;
    }

    /**
     * @return The element that was being processed that caused the error
     */
    public Optional<Subscriber.Element<?>> getElement() {
        return Optional.ofNullable(element);
    }
}
