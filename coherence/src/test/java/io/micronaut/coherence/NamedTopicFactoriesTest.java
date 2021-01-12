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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.*;

import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import data.Person;
import data.PhoneNumber;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "NamedTopicFactoriesTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    void shouldInjectIntoConstructor() {
        CtorBean bean = ctx.getBean(CtorBean.class);

        assertThat(bean.getNumbers(), Matchers.notNullValue());
        assertThat(bean.getNumbers().getName(), Matchers.is("numbers"));
    }

    @Test
    public void shouldInjectPublisher() throws Exception {
        NamedTopicPublisherFieldsBean publisherBean = ctx.getBean(NamedTopicPublisherFieldsBean.class);
        NamedTopicSubscriberFieldsBean subscriberBean = ctx.getBean(NamedTopicSubscriberFieldsBean.class);

        Publisher<Integer> numbersPublisher = publisherBean.getNumbers();
        assertThat(numbersPublisher, is(notNullValue()));

        Publisher<Person> peoplePublisher = publisherBean.getPeople();
        assertThat(peoplePublisher, is(notNullValue()));

        Subscriber<Integer> numbersSubscriber = subscriberBean.getNumbers();
        assertThat(numbersSubscriber, is(notNullValue()));

        Subscriber<Person> peopleSubscriber = subscriberBean.getPeople();
        assertThat(peopleSubscriber, is(notNullValue()));

        Subscriber<String> peopleFirstNamesSubscriber = subscriberBean.getPeopleFirstNames();
        assertThat(peopleFirstNamesSubscriber, is(notNullValue()));

        Subscriber<Person> peopleFilteredSubscriber = subscriberBean.getPeopleFiltered();
        assertThat(peopleFilteredSubscriber, is(notNullValue()));

        CompletableFuture<Subscriber.Element<Integer>> receiveNumber = numbersSubscriber.receive();
        numbersPublisher.send(19).join();
        Subscriber.Element<Integer> element = receiveNumber.get(1, TimeUnit.MINUTES);
        assertThat(element.getValue(), is(19));

        Person homer = new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999"));
        Person bart = new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999"));

        CompletableFuture<Subscriber.Element<Person>> receivePerson = peopleSubscriber.receive();
        CompletableFuture<Subscriber.Element<String>> receiveName = peopleFirstNamesSubscriber.receive();
        CompletableFuture<Subscriber.Element<Person>> receiveFiltered = peopleFilteredSubscriber.receive();

        peoplePublisher.send(homer).join();

        Subscriber.Element<Person> personElement = receivePerson.get(1, TimeUnit.MINUTES);
        Subscriber.Element<String> nameElement = receiveName.get(1, TimeUnit.MINUTES);
        assertThat(personElement.getValue(), is(homer));
        assertThat(nameElement.getValue(), is(homer.getFirstName()));

        assertThat(receiveFiltered.isDone(), is(false));
        peoplePublisher.send(bart).join();
        personElement = receiveFiltered.get(1, TimeUnit.MINUTES);
        assertThat(personElement.getValue(), is(bart));
    }


    // ----- test beans -----------------------------------------------------

    @Singleton
    @Requires(env = "NamedTopicFactoriesTest")
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
    @Requires(env = "NamedTopicFactoriesTest")
    static class NamedTopicPublisherFieldsBean {
        @Inject
        private Publisher<Person> people;

        @Inject
        @Name("numbers")
        private Publisher<Integer> numbersPublisher;

        public Publisher<Integer> getNumbers() {
            return numbersPublisher;
        }

        public Publisher<Person> getPeople() {
            return people;
        }
    }

    @Singleton
    @Requires(env = "NamedTopicFactoriesTest")
    static class NamedTopicSubscriberFieldsBean {
        @Inject
        private Subscriber<Person> people;

        @Inject
        @Name("numbers")
        private Subscriber<Integer> namedTopic;

        @Inject
        @Name("people")
        @PropertyExtractor("firstName")
        private Subscriber<String> peopleFirstNames;

        @Inject
        @Name("people")
        @WhereFilter("firstName == 'Bart'")
        private Subscriber<Person> peopleFiltered;

        public Subscriber<Integer> getNumbers() {
            return namedTopic;
        }

        public Subscriber<Person> getPeople() {
            return people;
        }

        public Subscriber<String> getPeopleFirstNames() {
            return peopleFirstNames;
        }

        public Subscriber<Person> getPeopleFiltered() {
            return peopleFiltered;
        }
    }

    @Singleton
    @Requires(env = "NamedTopicFactoriesTest")
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
    @Requires(env = "NamedTopicFactoriesTest")
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
