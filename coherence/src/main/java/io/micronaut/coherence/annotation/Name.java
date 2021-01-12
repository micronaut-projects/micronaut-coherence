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
 * A qualifier annotation used when injecting Coherence resource to indicate a
 * specific resource name.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
    /**
     * The name used to identify a specific resource.
     *
     * @return the name used to identify a specific resource
     */
    String value();

    /**
     * Return {@code true} to indicate whether name is a regular expression.
     *
     * @return {@code true} to indicate whether name is a regular expression
     */
    boolean regex() default false;

    /**
     * An annotation literal for the {@link Name} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<Name>
            implements Name {
        /**
         * The resource name.
         */
        private final String m_sName;
        /**
         * {@code true} to indicate whether name is a regular expression.
         */
        private final boolean m_fRegex;

        /**
         * Construct {@link Literal} instance.
         *
         * @param sName the resource name
         */
        private Literal(String sName) {
            this(sName, false);
        }

        /**
         * Construct {@link Literal} instance.
         *
         * @param sName  the resource name
         * @param fRegex {@code true} to indicate whether name is a regular expression
         */
        public Literal(String sName, boolean fRegex) {
            m_sName = sName;
            m_fRegex = fRegex;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sName the resource name
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sName) {
            return new Literal(sName);
        }

        /**
         * The name used to identify a specific resource.
         *
         * @return the name used to identify a specific resource
         */
        @Override
        public String value() {
            return m_sName;
        }

        /**
         * Return {@code true} to indicate whether name is a regular expression.
         *
         * @return {@code true} to indicate whether name is a regular expression
         */
        @Override
        public boolean regex() {
            return m_fRegex;
        }
    }
}
