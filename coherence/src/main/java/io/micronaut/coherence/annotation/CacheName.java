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
package io.micronaut.coherence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used to indicate a specific cache name.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheName {
    /**
     * Obtain the value used to identify a specific cache.
     *
     * @return value used to identify a specific cache
     */
    String value();

    /**
     * An annotation literal for the {@link CacheName} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<CacheName> implements CacheName {
        /**
         * The cache name.
         */
        private final String f_sName;

        /**
         * Construct {@link Literal} instance.
         *
         * @param sName the cache name
         */
        private Literal(String sName) {
            f_sName = sName;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sName the cache name
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sName) {
            return new Literal(sName);
        }

        /**
         * The name used to identify a specific cache.
         *
         * @return the name used to identify a specific cache
         */
        public String value() {
            return f_sName;
        }
    }
}
