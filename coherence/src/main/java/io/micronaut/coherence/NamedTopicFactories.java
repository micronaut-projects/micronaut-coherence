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
package io.micronaut.coherence;

import javax.inject.Named;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;
import com.oracle.coherence.inject.SubscriberGroup;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.InjectionPoint;

/**
 * A Micronaut factory for producing Coherence {@link com.tangosol.net.topic.NamedTopic},
 * {@link com.tangosol.net.topic.Publisher} and {@link com.tangosol.net.topic.Subscriber}
 * beans.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
class NamedTopicFactories {

    @Bean(preDestroy = "release")
    @Prototype
    @Named("SessionName")
    @Type(NamedTopic.class)
    <V> NamedTopic<V> getTopicFromSession(InjectionPoint<?> injectionPoint) {
        return getTopic(injectionPoint);
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Named("Name")
    @Type(NamedTopic.class)
    @Primary
    <V> NamedTopic<V> getTopic(InjectionPoint<?> injectionPoint) {
        return getTopicInternal(injectionPoint);
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Named("SessionName")
    @Type(Publisher.class)
    <V> Publisher<V> getPublisherFromSession(InjectionPoint<?> injectionPoint) {
        return getPublisher(injectionPoint);
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Named("Name")
    @Type(Publisher.class)
    @Primary
    <V> Publisher<V> getPublisher(InjectionPoint<?> injectionPoint) {
        NamedTopic<V> topic = getTopicInternal(injectionPoint);
        return topic.createPublisher();
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Named("SessionName")
    @Type(Subscriber.class)
    <V> Subscriber<V> getSubscriberFromSession(InjectionPoint<?> injectionPoint) {
        return getSubscriber(injectionPoint);
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Named("Name")
    @Type(Subscriber.class)
    @Primary
    @SuppressWarnings("unchecked")
    <V> Subscriber<V> getSubscriber(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        String groupName = metadata.getValue(SubscriberGroup.class, String.class).orElse(null);
        NamedTopic<V> topic = getTopicInternal(injectionPoint);
        return groupName == null
               ? topic.createSubscriber()
               : topic.createSubscriber(Subscriber.Name.of(groupName));
    }

    private <V> NamedTopic<V> getTopicInternal(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        String sessionName = metadata.getValue(SessionName.class, String.class).orElse(Coherence.DEFAULT_NAME);
        String name = metadata.getValue(Name.class, String.class).orElse(getName(injectionPoint));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine topic name. No @Name qualifier and injection point is not named");
        }

        Session session = Coherence.findSession(sessionName)
                .orElseThrow(() -> new IllegalStateException("No Session is configured with name " + sessionName));

        return session.getTopic(name);
    }

    /**
     * Returns the name of an injection point.
     *
     * @param injectionPoint the injection point to find the name of
     *
     * @return the name of an injection point
     */
    private String getName(InjectionPoint<?> injectionPoint) {
        if (injectionPoint instanceof io.micronaut.core.naming.Named) {
            return ((io.micronaut.core.naming.Named) injectionPoint).getName();
        }
        return null;
    }
}
