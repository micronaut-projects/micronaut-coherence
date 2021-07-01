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
package io.micronaut.coherence.messaging.binders;

import java.lang.annotation.Annotation;

/**
 * Interface for binders that bind method arguments from a {@link com.tangosol.net.topic.Subscriber.Element} via a annotation.
 *
 * @param <T> The target type
 * @param <A> The annotation type
 * @author Jonathan Knight
 * @since 1.0
 */
public interface AnnotatedElementBinder<A extends Annotation, T> extends ElementBinder<T> {

    /**
     * @return The annotation type
     */
    Class<A> annotationType();
}
