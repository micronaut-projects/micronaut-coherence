/*
 * Copyright 2017-2023 original authors
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

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link io.micronaut.coherence.annotation.FilterBinding} annotation representing an
 * {@link com.tangosol.util.filter.AlwaysFilter AlwaysFilter}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@FilterBinding
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AlwaysFilter {

    /**
     * An annotation literal for the {@link AlwaysFilter}
     * annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<AlwaysFilter> implements AlwaysFilter {

        /**
         * A {@link Literal} instance.
         */
        public static final Literal INSTANCE = new Literal();

        /**
         * Construct {@code Literal} instance.
         */
        private Literal() {
        }
    }
}
