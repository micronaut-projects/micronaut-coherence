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
 * A qualifier annotation used for any BACKLOG event.
 *
 * @author Aleks Seovic
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Backlog {
    /**
     * Obtain the type of backlog event.
     *
     * @return the type of backlog event
     */
    Type value();

    /**
     * The backlog event type.
     */
    enum Type {
        /**
         * Indicates that a participant was previously
         * backlogged but is no longer so.
         */
        NORMAL,

        /**
         * Indicates that a participant is backlogged; if
         * the participant is remote it indicates the
         * remote participant has more work than it can handle;
         * if the participant is local it indicates this
         * participant has more work than it can handle.
         */
        EXCESSIVE
    }

    /**
     * An annotation literal for the {@link Backlog} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<Backlog> implements Backlog {

        /**
         * The backlog event type.
         */
        private final Type type;

        /**
         * Construct {@link Literal} instance.
         *
         * @param type the backlog event type
         */
        private Literal(Type type) {
            this.type = type;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param type the backlog event type
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(Type type) {
            return new Literal(type);
        }

        /**
         * The backlog event type.
         *
         * @return the backlog event type
         */
        public Type value() {
            return type;
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
            return type == literal.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), type);
        }
    }
}
