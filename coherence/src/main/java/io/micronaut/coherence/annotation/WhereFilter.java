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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A {@link FilterBinding} annotation representing a {@link com.tangosol.util.Filter}
 * produced from a CohQL where clause.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Inherited
@FilterBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface WhereFilter {
    /**
     * The CohQL query expression.
     *
     * @return the CohQL query expression
     */
    String value();


    /**
     * An annotation literal for the {@link WhereFilter} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<WhereFilter> implements WhereFilter {
        /**
         * The CohQL query expression.
         */
        private final String f_sQuery;

        /**
         * Construct {@code Literal} instance.
         *
         * @param sQuery the CohQL query expression
         */
        private Literal(String sQuery) {
            this.f_sQuery = sQuery;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sQuery the CohQL query expression
         * @return a {@link Literal} with the specified CohQL query
         */
        public static Literal of(String sQuery) {
            return new Literal(sQuery);
        }

        /**
         * The CohQL query expression.
         *
         * @return the CohQL query expression
         */
        public String value() {
            return f_sQuery;
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
            return Objects.equals(f_sQuery, literal.f_sQuery);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), f_sQuery);
        }
    }
}
