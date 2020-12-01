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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tangosol.net.Coherence;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Subscriber;

import io.micronaut.coherence.annotation.CoherencePublisher;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "CoherencePublisherTest")
class CoherencePublisherTest {

    @Inject
    ApplicationContext context;

    @Inject
    Coherence coherence;

    @Inject
    PublishersOne publishersOne;

    @Test
    void shouldSendMessage() throws Exception {
        Subscriber<String> subscriber = getSubscriber("One");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing One...";
        publishersOne.send(message);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendMessageWithAsyncResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Two");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing Two...";
        CompletableFuture<Void> sent = publishersOne.sendAsync(message);

        sent.get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendMessageWithReactiveResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Three");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing Two...";
        Single<Void> sent = publishersOne.sendWithReactiveResponse(message);

        sent.toFuture().get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendSingleReactiveMessages() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Four");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing Reactive...";
        Observable<String> observable = Observable.fromArray(message);
        publishersOne.sendReactive(observable);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendAllReactiveMessages() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Four");
        CompletableFuture<Subscriber.Element<String>> future;

        future = subscriber.receive();

        Observable<String> observable = Observable.fromArray("One", "Two", "Three");
        publishersOne.sendReactive(observable);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("One"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Two"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Three"));
    }

    @Test
    void shouldSendSingleReactiveMessagesWithAsyncResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Five");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing Reactive...";
        Observable<String> observable = Observable.fromArray(message);
        CompletableFuture<Void> sendFuture = publishersOne.sendReactiveAsync(observable);

        sendFuture.get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendAllReactiveMessagesWithAsyncResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Five");
        CompletableFuture<Subscriber.Element<String>> future;

        future = subscriber.receive();

        Observable<String> observable = Observable.fromArray("One", "Two", "Three");
        CompletableFuture<Void> sendFuture = publishersOne.sendReactiveAsync(observable);

        sendFuture.get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("One"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Two"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Three"));
    }

    @Test
    void shouldSendSingleReactiveMessagesWithReactiveResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Six");
        CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

        String message = "Testing Reactive...";
        Observable<String> observable = Observable.fromArray(message);
        Single<Void> sent = publishersOne.sendReactiveWithReactiveResponse(observable);

        sent.toFuture().get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is(message));
    }

    @Test
    void shouldSendAllReactiveMessagesWithReactiveResponse() throws Exception {
        Subscriber<String> subscriber = getSubscriber("Six");
        CompletableFuture<Subscriber.Element<String>> future;

        future = subscriber.receive();

        Observable<String> observable = Observable.fromArray("One", "Two", "Three");
        Single<Void> sent = publishersOne.sendReactiveWithReactiveResponse(observable);
        sent.toFuture().get(1, TimeUnit.MINUTES);

        Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("One"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Two"));

        future = subscriber.receive();
        element = future.get(1, TimeUnit.MINUTES);
        assertThat(element, is(notNullValue()));
        assertThat(element.getValue(), is("Three"));
    }

    private Subscriber<String> getSubscriber(String name) {
        NamedTopic<String> topic = coherence.getSession().getTopic(name);
        return topic.createSubscriber();
    }

    @Singleton
    @CoherencePublisher
    @Requires(env = "CoherencePublisherTest")
    interface PublishersOne {
        @Topic("One")
        void send(String message);

        @Topic("Two")
        CompletableFuture<Void> sendAsync(String message);

        @Topic("Three")
        Single<Void> sendWithReactiveResponse(String message);

        @Topic("Four")
        void sendReactive(Observable<String> observable);

        @Topic("Five")
        CompletableFuture<Void> sendReactiveAsync(Observable<String> observable);

        @Topic("Six")
        Single<Void> sendReactiveWithReactiveResponse(Observable<String> observable);
    }
}
