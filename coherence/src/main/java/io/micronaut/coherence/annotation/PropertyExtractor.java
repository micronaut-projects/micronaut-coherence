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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A {@link ExtractorBinding} annotation representing a {@link
 * com.tangosol.util.extractor.UniversalExtractor}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Inherited
@ExtractorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PropertyExtractor.Extractors.class)
public @interface PropertyExtractor {
    /**
     * Returns the a method or property name to use when creating a {@link
     * com.tangosol.util.extractor.UniversalExtractor}.
     * <p>
     * If the value does not end in {@code "()"} the value is assumed to be a
     * property name. If the value is prefixed with one of the accessor prefixes
     * {@code "get"} or {@code "is"} and ends in {@code "()"} this extractor is
     * a property extractor. Otherwise, if the value just ends in {@code "()"}
     * this value is considered a method name.
     *
     * @return the value used for the where clause when creating a {@link
     * com.tangosol.util.extractor.UniversalExtractor}
     */
    String value();


    /**
     * A holder for the repeatable {@link PropertyExtractor} annotation.
     */
    @Inherited
    @ExtractorBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface Extractors {

        PropertyExtractor[] value();

        /**
         * An annotation literal for the {@link Extractors} annotation.
         */
        @SuppressWarnings("ClassExplicitlyAnnotation")
        class Literal extends AbstractArrayLiteral<Extractors> implements Extractors {
            /**
             * Construct {@code Literal} instance.
             *
             * @param aExtractors the extractors
             */
            private Literal(PropertyExtractor... aExtractors) {
                super(aExtractors);
            }

            /**
             * Create an {@link Literal}.
             *
             * @param aExtractors the extractors
             * @return an {@link Literal} containing the specified
             * extractors
             */
            public static Literal of(PropertyExtractor... aExtractors) {
                return new Literal(aExtractors);
            }

            /**
             * Obtain the extractor annotations contained in this annotation.
             *
             * @return the extractor annotations contained in this annotation
             */
            public PropertyExtractor[] value() {
                return (PropertyExtractor[]) array;
            }
        }
    }


    /**
     * An annotation literal for the {@link PropertyExtractor} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AnnotationLiteral<PropertyExtractor> implements PropertyExtractor {
        /**
         * The name of the property to extract.
         */
        private final String f_sPropertyName;

        /**
         * Construct {@code Literal} instance.
         *
         * @param sPropertyName the name of the property to extract
         */
        private Literal(String sPropertyName) {
            f_sPropertyName = sPropertyName;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sPropertyName the name of the property to extract
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sPropertyName) {
            return new Literal(sPropertyName);
        }

        /**
         * The name of the property to extract.
         *
         * @return the name of the property to extract
         */
        public String value() {
            return f_sPropertyName;
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
            return Objects.equals(f_sPropertyName, literal.f_sPropertyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), f_sPropertyName);
        }
    }
}
