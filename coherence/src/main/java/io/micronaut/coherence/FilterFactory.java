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

import com.tangosol.util.Filter;

import java.lang.annotation.Annotation;

/**
 * A factory that produces instances of {@link Filter} for a
 * given {@link Annotation}.
 * <p>
 * A {@link FilterFactory} is normally a CDI bean that is also annotated with a
 * {@link io.micronaut.coherence.annotation.FilterBinding} annotation. Whenever an injection point annotated with
 * the corresponding {@link io.micronaut.coherence.annotation.FilterBinding} annotation is encountered the {@link
 * FilterFactory} bean's {@link FilterFactory#create(Annotation)}
 * method is called to create an instance of a {@link Filter}.
 *
 * @param <A> the annotation type that the factory supports
 * @param <T> the type of value being filtered
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public interface FilterFactory<A extends Annotation, T> {
    /**
     * Create a {@link Filter} instance.
     *
     * @param annotation the {@link Annotation} that defines the filter
     * @return a {@link Filter} instance
     */
    Filter<T> create(A annotation);
}
