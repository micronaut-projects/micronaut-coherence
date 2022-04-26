/*
 * Copyright 2022 original authors
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

import com.tangosol.net.topic.Subscriber;
import io.micronaut.coherence.messaging.exceptions.CoherenceSubscriberException;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link CoherenceSubscriberException}.
 */
class CoherenceSubscriberExceptionTest {
    @Test
    void testFourArgMessageCtor() {
        Subscriber<?> subscriber = mock(Subscriber.class);
        Subscriber.Element<?> element = mock(Subscriber.Element.class);

        CoherenceSubscriberException exception =
                new CoherenceSubscriberException("message", Integer.MAX_VALUE, subscriber, element);

        assertThat(exception.getMessage(), is("message"));
        assertThat(exception.getKafkaListener(), is(Integer.MAX_VALUE));
        assertThat(exception.getKafkaConsumer(), is(subscriber));

        Optional<Subscriber.Element<?>> optElement = exception.getElement();
        assertThat(optElement, notNullValue());
        assertThat(optElement.orElseThrow(AssertionFailedError::new), is(element));
    }

    @Test
    void testFiveArgCtor() {
        Subscriber<?> subscriber = mock(Subscriber.class);
        Subscriber.Element<?> element = mock(Subscriber.Element.class);
        Throwable cause = new RuntimeException();

        CoherenceSubscriberException exception =
                new CoherenceSubscriberException("message", cause, Integer.MAX_VALUE, subscriber, element);

        assertThat(exception.getMessage(), is("message"));
        assertThat(exception.getCause(), is(cause));
        assertThat(exception.getKafkaListener(), is(Integer.MAX_VALUE));
        assertThat(exception.getKafkaConsumer(), is(subscriber));

        Optional<Subscriber.Element<?>> optElement = exception.getElement();
        assertThat(optElement, notNullValue());
        assertThat(optElement.orElseThrow(AssertionFailedError::new), is(element));
    }

    @Test
    void testFourArgCauseCtor() {
        Subscriber<?> subscriber = mock(Subscriber.class);
        Subscriber.Element<?> element = mock(Subscriber.Element.class);
        Throwable cause = new RuntimeException();

        CoherenceSubscriberException exception =
                new CoherenceSubscriberException(cause, Integer.MAX_VALUE, subscriber, element);

        assertThat(exception.getMessage(), nullValue());
        assertThat(exception.getCause(), is(cause));
        assertThat(exception.getKafkaListener(), is(Integer.MAX_VALUE));
        assertThat(exception.getKafkaConsumer(), is(subscriber));

        Optional<Subscriber.Element<?>> optElement = exception.getElement();
        assertThat(optElement, notNullValue());
        assertThat(optElement.orElseThrow(AssertionFailedError::new), is(element));
    }

    @Test
    void testNullElementArg() {
        Subscriber<?> subscriber = mock(Subscriber.class);

        CoherenceSubscriberException exception =
                new CoherenceSubscriberException("message", Integer.MAX_VALUE, subscriber, null);

        assertThat(exception.getMessage(), is("message"));
        assertThat(exception.getKafkaListener(), is(Integer.MAX_VALUE));
        assertThat(exception.getKafkaConsumer(), is(subscriber));

        Optional<Subscriber.Element<?>> optElement = exception.getElement();
        assertThat(optElement, notNullValue());
        assertThat(optElement.isPresent(), is(false));
    }
}
