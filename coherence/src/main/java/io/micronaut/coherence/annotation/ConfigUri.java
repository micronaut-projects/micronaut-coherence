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
import java.util.Objects;

/**
 * A qualifier annotation used to indicate a Coherence configuration resource URI.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigUri {
    /**
     * The URI used to identify a specific config resource.
     *
     * @return the URI used to identify a specific config resource
     */
    String value();

    /**
     * An annotation literal for the {@link ConfigUri} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<ConfigUri> implements ConfigUri {
        /**
         * The config resource URI.
         */
        private final String m_sURI;

        /**
         * Construct {@link Literal} instance.
         *
         * @param sURI the config resource URI
         */
        private Literal(String sURI) {
            m_sURI = sURI;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sURI the config resource URI
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sURI) {
            return new Literal(sURI);
        }

        /**
         * The name used to identify a specific resource.
         *
         * @return the name used to identify a specific resource
         */
        public String value() {
            return m_sURI;
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
            return Objects.equals(m_sURI, literal.m_sURI);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), m_sURI);
        }
    }
}
