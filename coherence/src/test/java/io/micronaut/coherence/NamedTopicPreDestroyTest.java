/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package io.micronaut.coherence;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;

import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "NamedTopicPreDestroyTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamedTopicPreDestroyTest {

    @Inject
    ApplicationContext ctx;

    //@Test
    void shouldClosePublisherOnScopeDeactivation() {
        Publishers publishers = ctx.getBean(Publishers.class);
        Publisher<String> publisher = publishers.getPublisher();
        Publisher<String> qualifiedPublisher = publishers.getQualifiedPublisher();

        AtomicBoolean publisherClosed = new AtomicBoolean(false);
        AtomicBoolean qualifiedClosed = new AtomicBoolean(false);

        Subscribers subscribers = ctx.getBean(Subscribers.class);
        Subscriber<String> subscriber = subscribers.getSubscriber();
        Subscriber<String> qualifiedSubscriber = subscribers.getQualifiedSubscriber();

        assertThat(subscriber.isActive(), is(true));
        assertThat(qualifiedSubscriber.isActive(), is(true));

        publisher.onClose(() -> publisherClosed.set(true));
        qualifiedPublisher.onClose(() -> qualifiedClosed.set(true));

        ctx.close();

        assertThat(publisherClosed.get(), is(true));
        assertThat(qualifiedClosed.get(), is(true));
        assertThat(subscriber.isActive(), is(false));
        assertThat(qualifiedSubscriber.isActive(), is(false));
    }

    // ----- test beans -----------------------------------------------------

    @Singleton
    @Requires(env = "NamedTopicPreDestroyTest")
    static class Subscribers {
        @Inject
        private Subscriber<String> numbers;

        @Inject
        @Name("numbers")
        private Subscriber<String> qualifiedSubscriber;

        Subscriber<String> getQualifiedSubscriber() {
            return qualifiedSubscriber;
        }

        Subscriber<String> getSubscriber() {
            return numbers;
        }
    }

    @Singleton
    @Requires(env = "NamedTopicPreDestroyTest")
    static class Publishers {
        @Inject
        private Publisher<String> numbers;

        @Inject
        @Name("numbers")
        private Publisher<String> qualifiedPublisher;

        Publisher<String> getPublisher() {
            return numbers;
        }

        Publisher<String> getQualifiedPublisher() {
            return qualifiedPublisher;
        }
    }
}
