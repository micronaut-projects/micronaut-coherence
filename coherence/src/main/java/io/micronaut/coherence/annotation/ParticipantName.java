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
 * A qualifier annotation used to indicate a specific participant name.
 *
 * @author Aleks Seovic
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ParticipantName {
    /**
     * The participant name.
     *
     * @return the participant name
     */
    String value();

    /**
     * An annotation literal for the {@link ParticipantName} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<ParticipantName> implements ParticipantName {
        /**
         * The participant name.
         */
        private final String name;

        /**
         * Construct {@link Literal} instance.
         *
         * @param name the participant name
         */
        private Literal(String name) {
            this.name = name;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param name the participant name
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String name) {
            return new Literal(name);
        }

        /**
         * The participant name.
         *
         * @return the participant name
         */
        public String value() {
            return name;
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
            return Objects.equals(name, literal.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }
    }
}
