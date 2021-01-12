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

import com.tangosol.util.ValueExtractor;

import java.lang.annotation.Annotation;

/**
 * A factory that produces instances of {@link ValueExtractor} for a given {@link Annotation}.
 * <p>
 * A {@link ExtractorFactory} is normally a CDI
 * bean that is also annotated with a {@link io.micronaut.coherence.annotation.ExtractorBinding}
 * annotation. Whenever an injection point annotated with the corresponding
 * {@link io.micronaut.coherence.annotation.ExtractorBinding} annotation is encountered the
 * {@link ExtractorFactory} bean's {@link ExtractorFactory#create(Annotation)} method is called to
 * create an instance of a {@link ValueExtractor}.
 *
 * @param <A> the annotation type that the factory supports
 * @param <T> the type of the value to extract from
 * @param <E> the type of value that will be extracted
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public interface ExtractorFactory<A extends Annotation, T, E> {
    /**
     * Create a {@link ValueExtractor} instance.
     *
     * @param annotation the {@link Annotation} that
     *                   defines the ValueExtractor
     * @return a {@link ValueExtractor} instance
     */
    ValueExtractor<T, E> create(A annotation);
}
