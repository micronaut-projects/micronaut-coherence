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
 * A qualifier annotation used to indicate a specific map name.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MapName {
    /**
     * Obtain the value used to identify a specific map.
     *
     * @return value used to identify a specific map
     */
    String value();

    /**
     * An annotation literal for the {@link MapName} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<MapName> implements MapName {
        /**
         * The map name.
         */
        private final String f_sName;

        /**
         * Construct {@link Literal} instance.
         *
         * @param sName the map name
         */
        private Literal(String sName) {
            f_sName = sName;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sName the map name
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sName) {
            return new Literal(sName);
        }

        /**
         * The name used to identify a specific map.
         *
         * @return the name used to identify a specific map
         */
        public String value() {
            return f_sName;
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
            return Objects.equals(f_sName, literal.f_sName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), f_sName);
        }
    }
}
