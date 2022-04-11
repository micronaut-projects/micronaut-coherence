/*
 * Copyright 2017-2022 original authors
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

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.Objects;
import java.util.stream.Stream;

import io.micronaut.context.Qualifier;
import io.micronaut.inject.BeanType;

/**
 * A qualifier for annotated factories.
 *
 * @param <T> the type of the factory class
 *
 * @author Jonathan Knight
 * @since 1.0
 */
class FactoryQualifier<T> implements Qualifier<T> {
    /**
     * The annotation type to look for.
     */
    private final Class<? extends Annotation> type;

    /**
     * If the annotation is a {@literal @}{@link java.lang.annotation.Repeatable} annotation
     * this is its holder annotation.
     */
    private final Class<? extends Annotation> holder;

    /**
     * Create a qualifier that matches the specific {@link FilterFactory} type.
     *
     * @param cls the {@link FilterFactory} to match
     */
    FactoryQualifier(Class<? extends Annotation> cls) {
        type = cls;
        Repeatable repeatable = cls.getAnnotation(Repeatable.class);
        holder = repeatable == null ? null : repeatable.value();
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        return candidates.filter(this::filter);
    }

    /**
     * Filter the candidate bean.
     *
     * @param bt the {@link BeanType} to filter
     *
     * @param <BT>  the type of the bean
     *
     * @return {@code true} if the bean type matches the filter
     */
    <BT extends BeanType<T>> boolean filter(BT bt) {
        if (holder == null) {
            // there is no holder, if we find the type it must have an annotations value of zero size
            return bt.isAnnotationPresent(type) && Objects.requireNonNull(bt.getAnnotation(type)).getAnnotations("value").isEmpty();
        }
        // there is a holder (i.e. type is @Repeatable), if we find the holder it must have an annotations value of
        // size 1
        return bt.isAnnotationPresent(holder) && Objects.requireNonNull(bt.getAnnotation(holder)).getAnnotations("value").size() == 1;
    }
}
