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

import com.tangosol.net.Coherence;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A qualifier annotation used when injecting Coherence resource to indicate
 * that those resource should be obtained from a specific {@link
 * com.tangosol.net.ConfigurableCacheFactory}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
    /**
     * Predefined constant for system scope.
     */
    String DEFAULT = Coherence.DEFAULT_SCOPE;
    /**
     * Predefined constant for system scope.
     */
    String SYSTEM = Coherence.SYSTEM_SCOPE;

    /**
     * The scope name or URI used to identify a specific {@link
     * com.tangosol.net.ConfigurableCacheFactory}.
     *
     * @return the scope name or URI used to identify a specific
     * {@link com.tangosol.net.ConfigurableCacheFactory}
     */
    String value() default DEFAULT;

    /**
     * An annotation literal for the {@link Scope} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<Scope> implements Scope {
        /**
         * The value for this literal.
         */
        private final String m_sValue;

        /**
         * Construct {@code Literal} instacne.
         *
         * @param sValue the scope name or URI used to identify a specific
         *               {@link com.tangosol.net.ConfigurableCacheFactory}
         */
        private Literal(String sValue) {
            m_sValue = sValue == null ? Scope.DEFAULT : sValue;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sValue the scope name or URI used to identify a specific
         *               {@link com.tangosol.net.ConfigurableCacheFactory}
         * @return a {@link Literal} with the specified URI
         */
        public static Literal of(String sValue) {
            return new Literal(sValue);
        }

        /**
         * Obtain the name value.
         *
         * @return the name value
         */
        public String value() {
            return m_sValue;
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
            return Objects.equals(m_sValue, literal.m_sValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), m_sValue);
        }
    }
}
