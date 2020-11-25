/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package io.micronaut.coherence;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;

import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml")
class NamedTopicFactoriesTest {

    @Inject
    ApplicationContext ctx;

    @Test
    void shouldInjectNamedTopicUsingFieldName() {
        NamedTopicFieldsBean bean = ctx.getBean(NamedTopicFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getName(), is("numbers"));
    }

    @Test
    void shouldInjectNamedTopicWithGenericValues() {
        NamedTopicFieldsBean bean = ctx.getBean(NamedTopicFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getName(), is("genericValues"));
    }

    @Test
    void shouldInjectNamedTopicWithGenerics() {
        NamedTopicFieldsBean bean = ctx.getBean(NamedTopicFieldsBean.class);
        assertThat(bean.getGenericTopic(), is(notNullValue()));
        assertThat(bean.getGenericTopic().getName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedNamedTopic() {
        NamedTopicFieldsBean bean = ctx.getBean(NamedTopicFieldsBean.class);
        assertThat(bean.getNamedTopic(), is(notNullValue()));
        assertThat(bean.getNamedTopic().getName(), is("numbers"));
    }

    @Test
    void shouldInjectTopicsFromDifferentSessions() throws Exception {
        DifferentSessionBean bean = ctx.getBean(DifferentSessionBean.class);

        NamedTopic<String> defaultTopic = bean.getDefaultCcfNumbers();
        NamedTopic<String> specificTopic = bean.getSpecificCcfNumbers();

        assertThat(defaultTopic, is(notNullValue()));
        assertThat(defaultTopic.getName(), Matchers.is("numbers"));

        assertThat(specificTopic, is(notNullValue()));
        assertThat(specificTopic.getName(), Matchers.is("numbers"));

        assertThat(defaultTopic, is(not(sameInstance(specificTopic))));

        Subscriber<String> defaultSubscriber = bean.getDefaultSubscriber();
        CompletableFuture<Subscriber.Element<String>> defaultFuture = defaultSubscriber.receive();
        Subscriber<String> specificSubscriber = bean.getSpecificSubscriber();
        CompletableFuture<Subscriber.Element<String>> specificFuture = specificSubscriber.receive();

        bean.getDefaultPublisher().send("value-one");
        bean.getSpecificPublisher().send("value-two");

        Subscriber.Element<String> valueOne = defaultFuture.get(1, TimeUnit.MINUTES);
        Subscriber.Element<String> valueTwo = specificFuture.get(1, TimeUnit.MINUTES);

        assertThat(valueOne.getValue(), is("value-one"));
        assertThat(valueTwo.getValue(), is("value-two"));
    }

    @Test
    void testCtorInjection() {
        CtorBean bean = ctx.getBean(CtorBean.class);

        assertThat(bean.getNumbers(), Matchers.notNullValue());
        assertThat(bean.getNumbers().getName(), Matchers.is("numbers"));
    }

    // ----- test beans -----------------------------------------------------

    @Singleton
    static class NamedTopicFieldsBean {
        @Inject
        private NamedTopic<String> numbers;

        @Inject
        @Name("numbers")
        private NamedTopic<String> namedTopic;

        @Inject
        @Name("numbers")
        private NamedTopic<Integer> genericTopic;

        @Inject
        private NamedTopic<List<String>> genericValues;

        public NamedTopic<Integer> getGenericTopic() {
            return genericTopic;
        }

        public NamedTopic<List<String>> getGenericValues() {
            return genericValues;
        }

        public NamedTopic<String> getNamedTopic() {
            return namedTopic;
        }

        public NamedTopic<String> getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class NamedTopicPublisherFieldsBean {
        @Inject
        private Publisher<String> numbers;

        @Inject
        @Name("numbers")
        private Publisher<String> namedTopic;

        @Inject
        @Name("numbers")
        private Publisher<Integer> genericTopic;

        @Inject
        private Publisher<List<String>> genericValues;

        public Publisher<Integer> getGenericPublisher() {
            return genericTopic;
        }

        public Publisher<List<String>> getGenericValuesPublisher() {
            return genericValues;
        }

        public Publisher<String> getNumbers() {
            return numbers;
        }

        public Publisher<String> getPublisher() {
            return namedTopic;
        }
    }

    @Singleton
    static class NamedTopicSubscriberFieldsBean {
        @Inject
        private Subscriber<String> numbers;

        @Inject
        @Name("numbers")
        private Subscriber<String> namedTopic;

        @Inject
        @Name("numbers")
        private Subscriber<Integer> genericTopic;

        @Inject
        private Subscriber<List<String>> genericValues;

        public Subscriber<Integer> getGenericSubscriber() {
            return genericTopic;
        }

        public Subscriber<List<String>> getGenericValuesSubscriber() {
            return genericValues;
        }

        public Subscriber<String> getNumbers() {
            return numbers;
        }

        public Subscriber<String> getSubscriber() {
            return namedTopic;
        }
    }

    @Singleton
    static class DifferentSessionBean {
        @Inject
        @Name("numbers")
        private NamedTopic<String> defaultCcfNumbers;

        @Inject
        @Name("numbers")
        private Publisher<String> defaultPublisher;

        @Inject
        @Name("numbers")
        private Subscriber<String> defaultSubscriber;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private NamedTopic<String> specificCcfNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private Publisher<String> specificPublisher;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private Subscriber<String> specificSubscriber;

        public NamedTopic<String> getDefaultCcfNumbers() {
            return defaultCcfNumbers;
        }

        public Publisher<String> getDefaultPublisher() {
            return defaultPublisher;
        }

        public Subscriber<String> getDefaultSubscriber() {
            return defaultSubscriber;
        }

        public NamedTopic<String> getSpecificCcfNumbers() {
            return specificCcfNumbers;
        }

        public Publisher<String> getSpecificPublisher() {
            return specificPublisher;
        }

        public Subscriber<String> getSpecificSubscriber() {
            return specificSubscriber;
        }
    }

    @Singleton
    static class CtorBean {
        private final NamedTopic<Integer> numbers;

        @Inject
        CtorBean(@Name("numbers") NamedTopic<Integer> topic) {
            this.numbers = topic;
        }

        NamedTopic<Integer> getNumbers() {
            return numbers;
        }
    }
}
