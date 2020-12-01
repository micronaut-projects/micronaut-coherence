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
package io.micronaut.coherence.messaging;

import java.util.Objects;

/**
 * A simple key to a {@link com.tangosol.net.topic.Publisher}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
class TopicKey {
    /**
     * The name of the topic.
     */
    private final String topicName;

    /**
     * The name of the owning session.
     */
    private final String sessionName;

    /**
     * Create a {@link TopicKey}.
     *
     * @param topicName     the name of the topic
     * @param sessionName   the name of the owning session
     */
    public TopicKey(String topicName, String sessionName) {
        this.topicName = topicName;
        this.sessionName = sessionName;
    }

    /**
     * Returns the topic name.
     * @return the topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Returns the session name.
     * @return the session name
     */
    public String getSessionName() {
        return sessionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TopicKey that = (TopicKey) o;
        return Objects.equals(topicName, that.topicName) &&
               Objects.equals(sessionName, that.sessionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, sessionName);
    }
}
