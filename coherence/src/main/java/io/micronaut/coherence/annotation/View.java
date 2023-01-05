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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A qualifier annotation used when injecting a {@link com.tangosol.net.cache.ContinuousQueryCache cache view}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
    /**
     * A flag that is {@code true} to cache both the keys and values of the
     * materialized view locally, or {@code false} to only cache the keys (the
     * default value is {@code true}).
     *
     * @return {@code true} to indicate that values should be cached or
     * {@code false} to indicate that only keys should be cached
     */
    boolean cacheValues() default true;


    /**
     * An annotation literal for the {@link View} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<View> implements View {
        /**
         * A singleton instance of {@link Literal}
         * with the cache values flag set to true.
         */
        public static final Literal INSTANCE = Literal.of(true);
        /**
         * A flag that is {@code true} to cache both the keys and values of the
         * materialized view locally, or {@code false} to only cache the keys.
         */
        private final boolean f_fCacheValues;

        /**
         * Construct {@code Literal} instance.
         *
         * @param fCacheValues a flag that is {@code true} to cache both the keys
         *                     and values of the materialized view locally, or
         *                     {@code false} to only cache the keys
         */
        private Literal(boolean fCacheValues) {
            this.f_fCacheValues = fCacheValues;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param fCacheValues a flag that is {@code true} to cache both the keys
         *                     and values of the materialized view locally, or
         *                     {@code false} to only cache the keys
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(boolean fCacheValues) {
            return new Literal(fCacheValues);
        }


        /**
         * Obtain the flag that is {@code true} to cache both the keys and
         * values of the materialized view locally, or {@code false} to only
         * cache the keys (the default value is {@code true}).
         *
         * @return {@code true} to indicate that values should be cache or
         * {@code false} to indicate that only keys should be cached.
         */
        @Override
        public boolean cacheValues() {
            return f_fCacheValues;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            final Literal literal = (Literal) o;
            return f_fCacheValues == literal.f_fCacheValues;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), f_fCacheValues);
        }
    }
}
