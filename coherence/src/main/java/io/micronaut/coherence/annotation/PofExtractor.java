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

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link ExtractorBinding} annotation representing a {@link
 * com.tangosol.util.extractor.PofExtractor}.
 * <p>
 * This annotation can be used to define an extractor that extracts and attribute
 * from a POF stream based on an array of integer property indices, in which
 * case the type is optional, or a property path based on serialized field names
 * concatenated using period (e.g. {@code address.city}) in which case  the {@link
 * #type()} attribute must be set as well.
 * <p>
 * The latter approach can only be used if the specified type is annotated with a
 * {@link com.tangosol.io.pof.schema.annotation.PortableType @PortableType} annotation and has been instrumented using
 * {@link com.tangosol.io.pof.generator.PortableTypeGenerator} (typically via {@code pof-maven-plugin}).
 * <p>
 * Either {@link #index()} or {@link #path()} must be specified within this
 * annotation in order for it to be valid.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Inherited
@ExtractorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PofExtractor.Extractors.class)
public @interface PofExtractor {
    /**
     * Returns an array of POF indexes to use to extract the value.
     *
     * @return an array of POF indexes to use to extract the value
     */
    int[] index() default {};

    /**
     * Returns a property path to use to extract the value.
     * <p>
     * This attribute can only be used in combination with the {@link #type()}
     * attribute, and only if the specified type is annotated with a
     * {@link com.tangosol.io.pof.schema.annotation.PortableType @PortableType}
     * annotation and instrumented using
     * {@link com.tangosol.io.pof.generator.PortableTypeGenerator}.
     *
     * @return a property path to use to extract the value
     */
    String path() default "";

    /**
     * Returns the root type to extract property from.
     *
     * @return the root type to extract property from
     */
    Class<?> type() default Object.class;

    /**
     * A holder for the repeatable {@link PofExtractor} annotation.
     */
    @Inherited
    @ExtractorBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface Extractors {

        /**
         * Return {@link PofExtractor POF extractors}.
         *
         * @return the {@link PofExtractor POF extractors}
         */
        PofExtractor[] value();

        /**
         * An annotation literal for the {@link Extractors} annotation.
         */
        @SuppressWarnings("ClassExplicitlyAnnotation")
        final class Literal extends io.micronaut.coherence.annotation.AnnotationLiteral<Extractors> implements Extractors {
            /**
             * The extractors value for this literal.
             */
            private final PofExtractor[] m_aExtractors;

            /**
             * Construct {@code Literal} instance.
             *
             * @param aExtractors the extractors
             */
            private Literal(PofExtractor... aExtractors) {
                m_aExtractors = aExtractors;
            }

            /**
             * Create an {@link Literal}.
             *
             * @param aExtractors the extractors
             * @return an {@link Literal} containing the specified
             * extractors
             */
            public static Literal of(PofExtractor... aExtractors) {
                return new Literal(aExtractors);
            }

            /**
             * The extractor annotations contained in this annotation.
             *
             * @return the extractor annotations contained in this annotation
             */
            public PofExtractor[] value() {
                return m_aExtractors;
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
                return Arrays.equals(m_aExtractors, literal.m_aExtractors);
            }

            @Override
            public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + Arrays.hashCode(m_aExtractors);
                return result;
            }
        }
    }

    /**
     * An annotation literal for the {@link PofExtractor}
     * annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<PofExtractor> implements PofExtractor {
        /**
         * The POF indexes to use to extract the value.
         */
        private final int[] f_anIndex;

        /**
         * The property path to use to extract the value.
         */
        private final String f_sPath;

        /**
         * The type being extracted.
         */
        private final Class<?> f_clzType;

        /**
         * Construct {@code Literal} instance.
         *
         * @param clzType the root type to extract property from
         * @param anIndex an array of POF indexes to use to extract the value
         * @param sPath   a property path to use to extract the value
         */
        private Literal(Class<?> clzType, int[] anIndex, String sPath) {
            f_clzType = clzType;
            f_anIndex = anIndex;
            f_sPath = sPath;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param value the POF indexes to use to extract the value
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(int... value) {
            return new Literal(Object.class, value, "");
        }

        /**
         * Create a {@link Literal}.
         *
         * @param type  the type of the extracted value
         * @param value the POF indexes to use to extract the value
         *
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(Class<?> type, int... value) {
            return new Literal(type, value, "");
        }

        /**
         * Create a {@link Literal}.
         *
         * @param type  the type of the extracted value
         * @param path  the POF indexes to use to extract the value
         *
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(Class<?> type, String path) {
            return new Literal(type, new int[]{}, path);
        }

        /**
         * The POF indexes to use to extract a value.
         *
         * @return the POF indexes to use to extract a value
         */
        public int[] index() {
            return f_anIndex;
        }

        /**
         * Returns a property path to use to extract the value.
         * <p>
         * This attribute can only be used in combination with the {@link #type()}
         * attribute, and only if the specified type is annotated with a
         * {@link com.tangosol.io.pof.schema.annotation.PortableType @PortableType} annotation and instrumented using
         * {@link com.tangosol.io.pof.generator.PortableTypeGenerator}.
         *
         * @return a property path to use to extract the value
         */
        public String path() {
            return f_sPath;
        }

        /**
         * The type being extracted.
         *
         * @return the type being extracted
         */
        public Class<?> type() {
            return f_clzType;
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
            return Arrays.equals(f_anIndex, literal.f_anIndex) &&
                    Objects.equals(f_sPath, literal.f_sPath) && Objects.equals(f_clzType, literal.f_clzType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(super.hashCode(), f_sPath, f_clzType);
            result = 31 * result + Arrays.hashCode(f_anIndex);
            return result;
        }
    }
}
