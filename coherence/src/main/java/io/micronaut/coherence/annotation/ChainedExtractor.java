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

/**
 * An {@link ExtractorBinding} annotation representing a
 * {@link com.tangosol.util.extractor.ChainedExtractor}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Inherited
@ExtractorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ChainedExtractor.Extractors.class)
public @interface ChainedExtractor {
    /**
     * Returns the method or property name to use when creating a {@link
     * com.tangosol.util.extractor.ChainedExtractor}.
     * <p>
     * If the value does not end in {@code "()"} the value is assumed to be a
     * property name. If the value is prefixed with one of the accessor prefixes
     * {@code "get"} or {@code "is"} and ends in {@code "()"} this extractor is
     * a property extractor. Otherwise, if the value just ends in {@code "()"}
     * this value is considered a method name.
     *
     * @return the value used for the where clause when creating a {@link
     * com.tangosol.util.extractor.ChainedExtractor}
     */
    String[] value();

    /**
     * A holder for the repeatable {@link ChainedExtractor} annotation.
     */
    @Inherited
    @ExtractorBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface Extractors {
        /**
         * An array of {@link ChainedExtractor}s.
         *
         * @return an array of {@link ChainedExtractor}s
         */
        ChainedExtractor[] value();

        /**
         * An annotation literal for the {@link Extractors} annotation.
         */
        @SuppressWarnings("ClassExplicitlyAnnotation")
        class Literal extends AbstractArrayLiteral<Extractors> implements Extractors {

            private Literal(ChainedExtractor... aExtractors) {
                super(aExtractors);
            }

            /**
             * Create an {@link Literal}.
             *
             * @param extractors the extractors
             * @return an {@link Literal} containing the specified
             * extractors
             */
            public static Literal of(ChainedExtractor... extractors) {
                return new Literal(extractors);
            }

            /**
             * The extractor annotations contained in this annotation.
             *
             * @return the extractor annotations contained in this annotation
             */
            public ChainedExtractor[] value() {
                return (ChainedExtractor[]) array;
            }
        }
    }

    /**
     * An annotation literal for the {@link ChainedExtractor} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    class Literal extends AbstractArrayLiteral<ChainedExtractor> implements ChainedExtractor {
        /**
         * Construct {@code Literal} instance.
         *
         * @param asFields the value used to create the extractor
         */
        private Literal(String[] asFields) {
            super(asFields);
        }

        /**
         * Create a {@link Literal}.
         *
         * @param asFields the value used to create the extractor
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String... asFields) {
            return new Literal(asFields);
        }

        /**
         * The value used for the where clause when creating a {@link
         * com.tangosol.util.extractor.ChainedExtractor}.
         *
         * @return the value used for the where clause when creating a {@link
         * com.tangosol.util.extractor.ChainedExtractor}
         */
        public String[] value() {
            return (String[]) array;
        }
    }
}
