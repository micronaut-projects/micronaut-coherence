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

import io.micronaut.coherence.messaging.exceptions.CoherenceSubscriberException;

/**
 * Interface that {@link io.micronaut.coherence.annotation.CoherenceTopicListener} beans can implement to handle exceptions.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public interface SubscriberExceptionHandler {
    /**
     * Handle the given exception.
     *
     * @param exception The exception to handle
     * @return {@code true} to continue processing messages, or {@code false} to close the subscriber.
     *
     */
    Action handle(CoherenceSubscriberException exception);

    /**
     * An enumeration of possible actions to take after handling an exception.
     */
    enum Action {
        /**
         * Continue to receive further messages.
         */
        Continue,
        /**
         * Close the subscriber and stop receiving messages.
         */
        Stop
    }
}
