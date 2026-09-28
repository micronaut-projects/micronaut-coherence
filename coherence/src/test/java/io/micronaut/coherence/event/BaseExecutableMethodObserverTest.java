/*
 * Copyright 2017-2026 original authors
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

import java.lang.annotation.Annotation;
import java.util.Set;

import com.tangosol.util.MapEvent;

import io.micronaut.coherence.annotation.Inserted;
import io.micronaut.coherence.annotation.Lite;
import io.micronaut.coherence.annotation.MapName;
import io.micronaut.coherence.annotation.ScopeName;
import io.micronaut.coherence.annotation.ServiceName;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.Synchronous;
import io.micronaut.coherence.annotation.Updated;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the qualifiers {@link BaseExecutableMethodObserver} observes.
 *
 * <p>An {@link ExecutableMethod}'s annotation metadata is an
 * {@link io.micronaut.inject.annotation.AnnotationMetadataHierarchy} of its declaring class and the method, so
 * the observed qualifiers have to be read from the method level only. Otherwise every class level annotation
 * of the bean (a {@code @SessionName} or {@code @ScopeName} shared by all of its methods, or a
 * {@code @MapName} of an unrelated {@code @CoherenceTopicListener} or {@code @CoherencePublisher} on the same
 * class) silently becomes a qualifier of every observer method.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseExecutableMethodObserverTest {

    private static ApplicationContext ctx;

    @BeforeAll
    static void startContext() {
        ctx = ApplicationContext.run("BaseExecutableMethodObserverTest");
    }

    @AfterAll
    static void stopContext() {
        if (ctx != null) {
            ctx.close();
        }
    }

    @Test
    void shouldObserveTheMethodLevelQualifiers() {
        Set<Annotation> qualifiers = observedQualifiers("onOrderInserted");

        assertThat(hasAnnotation(qualifiers, Inserted.class), is(true));
        assertThat(mapNames(qualifiers), is(Set.of("orders")));
    }

    @Test
    void shouldNotObserveClassLevelQualifiers() {
        Set<Annotation> qualifiers = observedQualifiers("onOrderInserted");

        // the class level annotations of QualifiedBean must not become qualifiers of its observer methods
        assertThat(mapNames(qualifiers).contains("class-level-map"), is(false));
        assertThat(hasAnnotation(qualifiers, SessionName.class), is(false));
        assertThat(hasAnnotation(qualifiers, ScopeName.class), is(false));
        assertThat(hasAnnotation(qualifiers, ServiceName.class), is(false));
        assertThat(hasAnnotation(qualifiers, Lite.class), is(false));
        assertThat(hasAnnotation(qualifiers, Synchronous.class), is(false));
    }

    @Test
    void shouldLeaveAnUnqualifiedMethodUnqualified() {
        Set<Annotation> qualifiers = observedQualifiers("onAnyUpdated");

        assertThat(hasAnnotation(qualifiers, Updated.class), is(true));
        // without a method level @MapName the observer is a wild-card listener, not a listener of the cache
        // named by the class level @MapName
        assertThat(mapNames(qualifiers), is(Set.of()));
    }

    private Set<Annotation> observedQualifiers(String methodName) {
        BeanDefinition<QualifiedBean> definition = ctx.getBeanDefinition(QualifiedBean.class);
        ExecutableMethod<QualifiedBean, ?> method = definition.getExecutableMethods()
                .stream()
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No executable method " + methodName));

        return new ExecutableMethodEventObserver<>(() -> null, method, null).getObservedQualifiers();
    }

    private static boolean hasAnnotation(Set<Annotation> qualifiers, Class<? extends Annotation> type) {
        return qualifiers.stream().anyMatch(type::isInstance);
    }

    private static Set<String> mapNames(Set<Annotation> qualifiers) {
        return qualifiers.stream()
                .filter(MapName.class::isInstance)
                .map(a -> ((MapName) a).value())
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * A bean with class level Coherence qualifiers and {@link Executable} methods. Plain {@code @Executable}
     * is used rather than {@code @CoherenceEventListener} so that the annotation metadata shape under test is
     * produced without starting a Coherence session.
     */
    @Singleton
    @Requires(env = "BaseExecutableMethodObserverTest")
    @MapName("class-level-map")
    @SessionName("class-level-session")
    @ScopeName("class-level-scope")
    @ServiceName("class-level-service")
    @Lite
    @Synchronous
    static class QualifiedBean {

        @Executable
        @Inserted
        @MapName("orders")
        void onOrderInserted(MapEvent<?, ?> event) {
        }

        @Executable
        @Updated
        void onAnyUpdated(MapEvent<?, ?> event) {
        }
    }
}
