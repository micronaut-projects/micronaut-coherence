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
package io.micronaut.coherence;

import io.micronaut.coherence.annotation.*;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
    private static final Logger LOG = LoggerFactory.getLogger(NamedTopicFactories.class);

    /**
     * The micronaut bean context.
     */
    private final BeanContext beanContext;

    /**
     * The filter factory for use when creating {@link com.tangosol.util.Filter Filters}.
     */
    private final FilterFactories filterFactory;

    /**
     * The extractor factory for use when creating {@link com.tangosol.util.ValueExtractor ValueExtractors}.
     */
    private final ExtractorFactories extractorFactory;

    NamedTopicFactories(BeanContext beanContext, FilterFactories filterFactory, ExtractorFactories extractorFactory) {
        this.beanContext = beanContext;
        this.filterFactory = filterFactory;
        this.extractorFactory = extractorFactory;
    }

    @Bean(preDestroy = "release")
    @Prototype
    @Type(NamedTopic.class)
    <V> NamedTopic<V> getTopic(InjectionPoint<?> injectionPoint) {
        return getTopicInternal(injectionPoint);
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Type(Publisher.class)
    <V> Publisher<V> getPublisher(InjectionPoint<?> injectionPoint) {
        NamedTopic<V> topic = getTopicInternal(injectionPoint);
        return topic.createPublisher();
    }

    @Bean(preDestroy = "close")
    @Prototype
    @Type(Subscriber.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    <V> Subscriber<V> getSubscriber(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        List<Subscriber.Option> options = new ArrayList<>();
        String groupName = metadata.getValue(SubscriberGroup.class, String.class).orElse(null);
        if (StringUtils.isNotEmpty(groupName)) {
            options.add(Subscriber.Name.of(groupName));
        }
        if (metadata.hasStereotype(FilterBinding.class)) {
            Filter filter = filterFactory.filter(injectionPoint);
            options.add(Subscriber.Filtered.by(filter));
        }
        if (metadata.hasStereotype(ExtractorBinding.class)) {
            ValueExtractor extractor = extractorFactory.extractor(injectionPoint);
            options.add(Subscriber.Convert.using(extractor));
        }
        NamedTopic<V> topic = getTopicInternal(injectionPoint);
        return options.isEmpty()
               ? topic.createSubscriber()
               : topic.createSubscriber(options.toArray(new Subscriber.Option[0]));
    }

    private <V> NamedTopic<V> getTopicInternal(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        String sessionName = metadata.getValue(SessionName.class, String.class).orElse(Coherence.DEFAULT_NAME);
        String name = metadata.getValue(Name.class, String.class).orElse(getName(injectionPoint));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine topic name. No @Name qualifier and injection point is not named");
        }

        try {
            Session session = beanContext.createBean(Session.class, sessionName);

            return session.getTopic(name);
        } catch (Throwable t) {
            LOG.error("Error getting NamedTopic " + name + " from session " + sessionName, t);
            throw new IllegalStateException("Failed getting NamedTopic " + name + " from session " + sessionName);
        }
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
