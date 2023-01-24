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
package io.micronaut.coherence.annotation;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.collection.ArrayMatching.arrayContainingInAnyOrder;


/**
 * @author Jonathan Knight
 * @since 1.0
 */
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilsTest {

    @Inject
    ApplicationContext context;

    @Test
    void shouldHaveNoNames() {
        String[] topicNames = getNames(NoAnnotations.class);
        assertThat(topicNames, is(arrayWithSize(0)));
    }

    @Test
    void shouldHaveAnnotationWithNoNames() {
        String[] topicNames = getNames(OneTopicNoNames.class);
        assertThat(topicNames, is(arrayWithSize(0)));
    }

    @Test
    void shouldHaveAnnotationWithOneName() {
        String[] topicNames = getNames(OneTopicOneName.class);
        assertThat(topicNames, is(arrayContainingInAnyOrder("Foo")));
    }

    @Test
    void shouldHaveAnnotationWithMultipleNames() {
        String[] topicNames = getNames(OneTopicMultipleName.class);
        assertThat(topicNames, is(arrayContainingInAnyOrder("Foo", "Bar")));
    }

    @Test
    void shouldHaveMultipleAnnotationsWithSingleName() {
        String[] topicNames = getNames(MultipleTopicsOneName.class);
        assertThat(topicNames, is(arrayContainingInAnyOrder("Foo", "Bar")));
    }

    @Test
    void shouldHaveMultipleAnnotationsWithMultipleNames() {
        String[] topicNames = getNames(MultipleTopicsMultipleNames.class);
        assertThat(topicNames, is(arrayContainingInAnyOrder("Foo", "Bar", "One", "Two")));
    }

    private String[] getNames(Class<?> bean) {
        BeanDefinition<?> beanDefinition = context.getBeanDefinition(bean);
        AnnotationMetadata metadata = beanDefinition.getAnnotationMetadata();
        return Utils.getTopicNames(metadata);
    }

    @Singleton
    static class NoAnnotations {
    }

    @Singleton
    @Topic
    static class OneTopicNoNames {
    }

    @Singleton
    @Topic("Foo")
    static class OneTopicOneName {
    }

    @Singleton
    @Topic({"Foo", "Bar"})
    static class OneTopicMultipleName {
    }

    @Singleton
    @Topic("Foo")
    @Topic("Bar")
    static class MultipleTopicsOneName {
    }

    @Singleton
    @Topic({"Foo", "Bar"})
    @Topic({"One", "Two"})
    static class MultipleTopicsMultipleNames {
    }
}
