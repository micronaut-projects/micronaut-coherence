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

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.tangosol.io.Serializer;
import com.tangosol.net.topic.Position;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import io.micronaut.coherence.annotation.PropertyExtractor;
import io.micronaut.coherence.annotation.SubscriberGroup;
import io.micronaut.coherence.annotation.WhereFilter;

import com.tangosol.net.Coherence;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;

import com.tangosol.net.topic.Subscriber;
import data.Person;
import io.micronaut.coherence.annotation.CoherenceTopicListener;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.context.annotation.Requires;
import io.micronaut.messaging.annotation.SendTo;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

/**
 * @author Jonathan Knight
 * @since 1.0
 */
@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "CoherenceTopicListenerTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoherenceTopicListenerTest {

    @Inject
    Coherence coherence;

    @Inject
    ListenerOne listenerOne;

    @Inject
    ListenerTwo listenerTwo;

    @Inject
    ListenerThree listenerThree;

    @Inject
    ListenerFour listenerFour;

    @Inject
    ListenerFive listenerFive;

    @Inject
    CoherenceTopicListenerProcessor processor;

    @BeforeEach
    void setup() {
        // ensure that all subscriber methods are subscribed before the tests start
        // as subscription is async
        Eventually.assertDeferred(() -> processor.isSubscribed(), is(true));
    }

    @Test
    void shouldHaveSubscribed() throws Exception {
        String message = "message one";

        try (Publisher<String> publisher = getPublisher("One")) {
            publisher.publish(message);

            assertThat(listenerOne.latchOne.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messageOne, is(message));

            assertThat(listenerTwo.latchOne.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerTwo.messageOne, is(message));
        }
    }

    @Test
    void shouldHaveSubscribedWithGroups() throws Exception {
        NamedTopic<String> topic = coherence.getSession().getTopic("Two");
        assertThat(topic.getSubscriberGroups(), containsInAnyOrder("Foo", "Bar"));

        String message = "message two";
        try (Publisher<String> publisher = topic.createPublisher()) {
            publisher.publish(message);
            assertThat(listenerOne.latchTwoFoo.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messageTwoFoo, is(message));
            assertThat(listenerOne.latchTwoBar.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messageTwoBar, is(message));
        }
    }

    @Test
    void shouldHaveSubscribedWithFilter() throws Exception {
        try (Publisher<Person> publisher = getPublisher("Three")) {
            Person homer = new Person("Homer", "Simpson", LocalDate.now(), null);
            publisher.publish(new Person("Ned", "Flanders", LocalDate.now(), null));
            publisher.publish(homer);
            publisher.publish(new Person("Apu", "Nahasapeemapetilon", LocalDate.now(), null));

            assertThat(listenerOne.latchThree.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messageThree, is(notNullValue()));
            assertThat(listenerOne.messageThree, is(homer));
        }
    }

    @Test
    void shouldHaveSubscribedWithConverter() throws Exception {
        try (Publisher<Person> publisher = getPublisher("People")) {
            publisher.publish(new Person("Homer", "Simpson", LocalDate.now(), null));

            assertThat(listenerOne.latchPeopleConverted.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messagePeopleConverted, is(notNullValue()));
            assertThat(listenerOne.messagePeopleConverted, is("Homer"));
        }
    }

    @Test
    void shouldHaveSubscribedToTopicFromMethodName() throws Exception {
        try (Publisher<String> publisher = getPublisher("four")) {
            String message = "message four";
            publisher.publish(message);

            assertThat(listenerOne.latchFour.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerOne.messageFour, is(message));

            assertThat(listenerTwo.latchFour.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listenerTwo.messageFour, is(message));
        }
    }

    @Test
    void shouldSendListenerResultToTargetTopic() throws Exception {
        try (Publisher<String> publisher = getPublisher("Five");
             Subscriber<String> subscriber = getSubscriber("Six")) {

            CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

            String message = "message five";
            publisher.publish(message).join();

            Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is(message.toUpperCase()));
        }
    }

    @Test
    void shouldNotSendVoidResultToTargetTopic() throws Exception {
        try (Publisher<String> publisher = getPublisher("Seven");
             Subscriber<String> subscriber = getSubscriber("Eight")) {

            CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

            String message = "message seven";
            publisher.publish(message).get(1, TimeUnit.MINUTES);

            getPublisher("Eight").publish("No Message").get(1, TimeUnit.MINUTES);
            Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is("No Message"));
        }
    }

    @Test
    void shouldSendListenerResultToMultipleTargetTopics() throws Exception {
        try (Publisher<String> publisher = getPublisher("Nine");
             Subscriber<String> subscriber1 = getSubscriber("Ten");
             Subscriber<String> subscriber2 = getSubscriber("Eleven")) {

            CompletableFuture<Subscriber.Element<String>> future1 = subscriber1.receive();
            CompletableFuture<Subscriber.Element<String>> future2 = subscriber2.receive();

            String message = "message nine";
            publisher.publish(message);

            Subscriber.Element<String> element = future1.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is(message.toUpperCase()));

            element = future2.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is(message.toUpperCase()));
        }
    }

    @Test
    void shouldSendListenerAsyncResultToTargetTopic() throws Exception {
        try (Publisher<String> publisher = getPublisher("Twelve");
             Subscriber<String> subscriber = getSubscriber("Thirteen")) {

            CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

            String message = "message twelve";
            publisher.publish(message);

            Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is(message.toUpperCase()));
        }
    }

    @Test
    void shouldSendListenerSingleReactiveResultToTargetTopic() throws Exception {
        try (Publisher<String> publisher = getPublisher("Fourteen");
             Subscriber<String> subscriber = getSubscriber("Fifteen")) {

            CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

            String message = "message fourteen";
            publisher.publish(message);

            Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is(message.toUpperCase()));
        }
    }

    @Test
    void shouldSendListenerReactiveResultToTargetTopic() throws Exception {
        try (Publisher<String> publisher = getPublisher("Sixteen");
             Subscriber<Character> subscriber = getSubscriber("Seventeen")) {

            CompletableFuture<Subscriber.Element<Character>> future = subscriber.receive();

            String message = "ABC";
            publisher.publish(message);

            Subscriber.Element<Character> element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is('A'));

            future = subscriber.receive();
            element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is('B'));

            future = subscriber.receive();
            element = future.get(1, TimeUnit.MINUTES);
            assertThat(element, is(notNullValue()));
            assertThat(element.getValue(), is('C'));
        }
    }

    @Test
    public void shouldReceiveElementBinderArguments() throws Exception {
        int channel = 1;
        try (Publisher<String> publisher = getPublisher("Eighteen", Publisher.OrderBy.id(channel))) {
            String message = "ABC";
            Publisher.Status status = publisher.publish(message).get(1, TimeUnit.MINUTES);

            listenerFour.latch.await(1, TimeUnit.MINUTES);
            assertThat(listenerFour.lastElementOne, is(notNullValue()));
            assertThat(listenerFour.lastElementOne.getChannel(), is(status.getChannel()));
            assertThat(listenerFour.lastElementOne.getPosition(), is(status.getPosition()));
            assertThat(listenerFour.lastElementOne.getValue(), is(message));
            assertThat(listenerFour.lastElementTwo, is(notNullValue()));
            assertThat(listenerFour.lastElementTwo.getChannel(), is(status.getChannel()));
            assertThat(listenerFour.lastElementTwo.getPosition(), is(status.getPosition()));
            assertThat(listenerFour.lastElementTwo.getValue(), is(message));
            assertThat(listenerFour.lastSubscriberTwo, is(notNullValue()));
            assertThat(listenerFour.lastValueThree, is(message));
            assertThat(listenerFour.lastChannelFour, is(status.getChannel()));
            assertThat(listenerFour.lastValueFour, is(message));
            assertThat(listenerFour.lastPositionFive, is(status.getPosition()));
            assertThat(listenerFour.lastValueFive, is(message));
            assertThat(listenerFour.lastBinary, is(notNullValue()));
            Serializer serializer = publisher.getNamedTopic().getService().getSerializer();
            assertThat(ExternalizableHelper.fromBinary(listenerFour.lastBinary, serializer), is(message));
        }
    }

    @Test
    public void shouldReceiveMessagesForMultipleSubscribersInSameGroup() {
        AtomicInteger count = new AtomicInteger();

        try (Publisher<String> publisher = getPublisher("Nineteen", Publisher.OrderByValue.value(v -> count.getAndIncrement()))) {
            for (int i = 0; i < publisher.getChannelCount() * 2; i++) {
                publisher.publish("message-" + i);
            }

            int expected = count.get();
            Eventually.assertDeferred(() -> listenerFive.received(), is(expected));
            TreeSet<String> set = new TreeSet<>(listenerFive.listOne);
            set.addAll(listenerFive.listTwo);
            assertThat(set.size(), is(expected));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Publisher<T> getPublisher(String name, Publisher.Option... options) {
        NamedTopic<String> topic = coherence.getSession().getTopic(name);
        return (Publisher<T>) topic.createPublisher(options);
    }

    @SuppressWarnings("unchecked")
    private <T> Subscriber<T> getSubscriber(String name) {
        NamedTopic<String> topic = coherence.getSession().getTopic(name);
        return (Subscriber<T>) topic.createSubscriber();
    }

    @CoherenceTopicListener
    @Requires(env = "CoherenceTopicListenerTest")
    static class ListenerOne {
        private final CountDownLatch latchOne = new CountDownLatch(1);
        private String messageOne;
        private final CountDownLatch latchTwoFoo = new CountDownLatch(1);
        private String messageTwoFoo;
        private final CountDownLatch latchTwoBar = new CountDownLatch(1);
        private String messageTwoBar;
        private final CountDownLatch latchThree = new CountDownLatch(1);
        private Person messageThree;
        private final CountDownLatch latchPeopleConverted = new CountDownLatch(1);
        private String messagePeopleConverted;
        private final CountDownLatch latchFour = new CountDownLatch(1);
        private String messageFour;

        @Topic("One")
        void listenOne(String value) {
            messageOne = value;
            latchOne.countDown();
        }

        @Topic("Two")
        @SubscriberGroup("Foo")
        void listenTwoFoo(String value) {
            messageTwoFoo = value;
            latchTwoFoo.countDown();
        }

        @Topic("Two")
        @SubscriberGroup("Bar")
        void listenTwoBar(String value) {
            messageTwoBar = value;
            latchTwoBar.countDown();
        }

        @Topic("Three")
        @WhereFilter("lastName == 'Simpson'")
        void listenThree(Person value) {
            messageThree = value;
            latchThree.countDown();
        }

        @Topic("People")
        @PropertyExtractor("firstName")
        void listenThreeConverted(String value) {
            messagePeopleConverted = value;
            latchPeopleConverted.countDown();
        }

        /**
         * Receives messages sent to topic "four"
         */
        @SuppressWarnings("unused")
        void four(String value) {
            messageFour = value;
            latchFour.countDown();
        }
    }

    @Singleton
    @Requires(env = "CoherenceTopicListenerTest")
    static class ListenerTwo {
        private final CountDownLatch latchOne = new CountDownLatch(1);
        private String messageOne;
        private final CountDownLatch latchFour = new CountDownLatch(1);
        private String messageFour;

        @Topic("One")
        @CoherenceTopicListener
        void listenOne(String value) {
            messageOne = value;
            latchOne.countDown();
        }

        @CoherenceTopicListener
        void four(String value) {
            messageFour = value;
            latchFour.countDown();
        }
    }

    @Singleton
    @Requires(env = "CoherenceTopicListenerTest")
    static class ListenerThree {
        @Topic("Five")
        @SendTo("Six")
        @CoherenceTopicListener
        String toUpper(String value) {
            return value.toUpperCase();
        }

        @Topic("Seven")
        @SendTo("Eight")
        @CoherenceTopicListener
        void noSendTo(String value) {
        }

        @Topic("Nine")
        @SendTo({"Ten", "Eleven"})
        @CoherenceTopicListener
        String multiSendTo(String value) {
            return value.toUpperCase();
        }

        @Topic("Twelve")
        @SendTo("Thirteen")
        @CoherenceTopicListener
        CompletableFuture<String> asyncSendTo(String value) {
            return CompletableFuture.supplyAsync(value::toUpperCase);
        }

        @Topic("Fourteen")
        @SendTo("Fifteen")
        @CoherenceTopicListener
        Single<String> reactiveSingleSendTo(String value) {
            return Single.fromFuture(CompletableFuture.supplyAsync(value::toUpperCase));
        }

        @Topic("Sixteen")
        @SendTo("Seventeen")
        @CoherenceTopicListener
        Observable<Character> reactiveSendTo(String value) {
            List<Character> list = new ArrayList<>();
            for (char c : value.toCharArray()) {
                list.add(c);
            }
            return Observable.fromArray(list.toArray(new Character[0]));
        }
    }

    @Singleton
    @Requires(env = "CoherenceTopicListenerTest")
    static class ListenerFour {

        CountDownLatch latch = new CountDownLatch(6);
        Subscriber.Element<String> lastElementOne;
        Subscriber.Element<String> lastElementTwo;
        Subscriber<String> lastSubscriberTwo;
        String lastValueThree;
        String lastValueFour;
        int lastChannelFour;
        String lastValueFive;
        Position lastPositionFive;
        Binary lastBinary;

        @Topic("Eighteen")
        @SubscriberGroup("One")
        @CoherenceTopicListener
        void element(Subscriber.Element<String> element) {
            lastElementOne = element;
            latch.countDown();
        }

        @Topic("Eighteen")
        @SubscriberGroup("Two")
        @CoherenceTopicListener
        void subscriberAndElement(Subscriber<String> subscriber, Subscriber.Element<String> element) {
            lastElementTwo = element;
            lastSubscriberTwo = subscriber;
            latch.countDown();
        }

        @Topic("Eighteen")
        @SubscriberGroup("Three")
        @CoherenceTopicListener
        void value(String s) {
            lastValueThree = s;
            latch.countDown();
        }

        @Topic("Eighteen")
        @SubscriberGroup("Four")
        @CoherenceTopicListener
        void subscriberAndElement(int channel, String s) {
            lastChannelFour = channel;
            lastValueFour = s;
            latch.countDown();
        }

        @Topic("Eighteen")
        @SubscriberGroup("Five")
        @CoherenceTopicListener
        void subscriberAndElement(Position position, String s) {
            lastPositionFive = position;
            lastValueFive = s;
            latch.countDown();
        }

        @Topic("Eighteen")
        @SubscriberGroup("Six")
        @CoherenceTopicListener
        void binary(Binary binary) {
            lastBinary = binary;
            latch.countDown();
        }
    }

    @Singleton
    @Requires(env = "CoherenceTopicListenerTest")
    static class ListenerFive {

        private final List<String> listOne = new ArrayList<>();
        private final  List<String> listTwo = new ArrayList<>();
        private final  AtomicInteger count = new AtomicInteger();

        @Topic("Nineteen")
        @SubscriberGroup("test")
        @CoherenceTopicListener
        void one(String value) {
            listOne.add(value);
            count.incrementAndGet();
        }

        @Topic("Nineteen")
        @SubscriberGroup("test")
        @CoherenceTopicListener
        void two(String value) {
            listTwo.add(value);
            count.incrementAndGet();
        }

       int received() {
           return count.get();
       }
    }
}
