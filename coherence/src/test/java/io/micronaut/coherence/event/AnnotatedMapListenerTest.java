/*
 * Copyright 2023 original authors
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
package io.micronaut.coherence.event;


import io.micronaut.coherence.annotation.*;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AnnotatedMapListenerTest {

    @Test
    void testEquals() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);

        Created created = c.class.getAnnotation(Created.class);
        Destroyed destroyed = c.class.getAnnotation(Destroyed.class);
        Truncated truncated = c.class.getAnnotation(Truncated.class);
        ScopeName scopeName = c.class.getAnnotation(ScopeName.class);
        SessionName sessionName = c.class.getAnnotation(SessionName.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(created);
        value.add(destroyed);
        value.add(truncated);
        value.add(scopeName);
        value.add(sessionName);
        when(observer.getObservedQualifiers()).thenReturn(value);

        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());
        AnnotatedMapListener listener2 = new AnnotatedMapListener(observer, observer.getObservedQualifiers());

        assertThat(listener, is(listener));
        assertThat(listener, is(not("test")));
        assertThat(listener, is(listener2));
    }

    @Test
    void testComparable() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);
        ExecutableMethodMapListener observer2 = mock(ExecutableMethodMapListener.class);

        Created created = c.class.getAnnotation(Created.class);
        Destroyed destroyed = c.class.getAnnotation(Destroyed.class);
        Truncated truncated = c.class.getAnnotation(Truncated.class);
        ScopeName scopeName = c.class.getAnnotation(ScopeName.class);
        SessionName sessionName = c.class.getAnnotation(SessionName.class);

        Created created2 = c2.class.getAnnotation(Created.class);
        Destroyed destroyed2 = c2.class.getAnnotation(Destroyed.class);
        Truncated truncated2 = c2.class.getAnnotation(Truncated.class);
        ScopeName scopeName2 = c.class.getAnnotation(ScopeName.class);
        SessionName sessionName2 = c.class.getAnnotation(SessionName.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(created);
        value.add(destroyed);
        value.add(truncated);
        value.add(scopeName);
        value.add(sessionName);
        when(observer.getObservedQualifiers()).thenReturn(value);

        Set<Annotation> value2 = new java.util.HashSet<>();
        value.add(created2);
        value.add(destroyed2);
        value.add(truncated2);
        value.add(scopeName2);
        value.add(sessionName2);
        when(observer2.getObservedQualifiers()).thenReturn(value2);


        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());
        AnnotatedMapListener listener2 = new AnnotatedMapListener(observer, observer.getObservedQualifiers());
        AnnotatedMapListener listener3 = new AnnotatedMapListener(observer2, observer2.getObservedQualifiers());

        //noinspection EqualsWithItself
        assertThat(listener.compareTo(listener), is(0));
        assertThat(listener.compareTo(listener2), is(0));
        assertThat(listener.compareTo(listener3), is(1));
        assertThat(listener3.compareTo(listener), is(-1));
    }

    @Test
    public void testLite() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);

        @Lite
        final class c { }
        Lite lite = c.class.getAnnotation(Lite.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(lite);
        when(observer.getObservedQualifiers()).thenReturn(value);

        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());

        assertThat(listener.isLite(), is(true));
    }

    @Test
    public void testSynchronous() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);

        @Synchronous
        final class c { }
        Synchronous synchronous = c.class.getAnnotation(Synchronous.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(synchronous);
        when(observer.getObservedQualifiers()).thenReturn(value);

        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());

        assertThat(listener.isSynchronous(), is(true));
    }

    @Test
    public void testWildCardServiceName() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);

        @ServiceName("*")
        final class c { }
        ServiceName serviceName = c.class.getAnnotation(ServiceName.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(serviceName);
        when(observer.getObservedQualifiers()).thenReturn(value);

        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());

        assertThat(listener.isWildCardServiceName(), is(true));
    }

    @Test
    public void testWildCardCacheName() {
        ExecutableMethodMapListener observer = mock(ExecutableMethodMapListener.class);

        @CacheName("*")
        final class c { }
        CacheName cacheName = c.class.getAnnotation(CacheName.class);

        Set<Annotation> value = new java.util.HashSet<>();
        value.add(cacheName);
        when(observer.getObservedQualifiers()).thenReturn(value);

        AnnotatedMapListener listener = new AnnotatedMapListener(observer, observer.getObservedQualifiers());

        assertThat(listener.isWildCardCacheName(), is(true));
    }

    @Created
    @Destroyed
    @Truncated
    @ScopeName("test")
    @SessionName("test")
    static final class c { }

    @Created
    @Destroyed
    @Truncated
    @ScopeName("test2")
    @SessionName("test2")
    final class c2 { }
}
