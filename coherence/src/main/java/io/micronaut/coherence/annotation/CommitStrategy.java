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
package io.micronaut.coherence.annotation;

/**
 * <p>An enum representing different strategies for committing positions in a Coherence topic when
 * using {@link CoherenceTopicListener}.</p>
 * <p>To track messages that have been consumed, Coherence allows committing positions at a frequency desired by
 * the developer.</p>
 * <p>Depending on requirements you may wish the commit more or less frequently and you may not care whether the
 * commit was successful or not. This enum allows configuring a range of policies for a Coherence topic subscriber
 * from leaving it down to the client to synchronously commit (with {@link #SYNC}) or asynchronously commit
 * (with {@link #ASYNC}) after each message is consumed, through to manually handling commits (with {@link #MANUAL}).</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public enum CommitStrategy {
    /**
     * Do not commit messages. In this case the subscriber method should accept an argument that is the {@link com.tangosol.net.topic.Subscriber.Element}
     * itself and call {@link com.tangosol.net.topic.Subscriber.Element#commit()} or {@link com.tangosol.net.topic.Subscriber.Element#commitAsync()}
     * to commit the received element.
     */
    MANUAL,
    /**
     * Synchronously commit using {@link com.tangosol.net.topic.Subscriber.Element#commit()} after each message is processed.
     */
    SYNC,
    /**
     * Asynchronously commit using {@link com.tangosol.net.topic.Subscriber.Element#commitAsync()} after each message is processed.
     */
    ASYNC,
}
