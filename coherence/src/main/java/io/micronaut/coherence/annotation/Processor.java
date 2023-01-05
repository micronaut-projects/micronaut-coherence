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

import com.tangosol.util.InvocableMap;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A qualifier annotation used to annotate the parameter {@link CoherenceEventListener} annotated methods
 * that will receive {@link com.tangosol.net.events.partition.cache.EntryProcessorEvent EntryProcessorEvents}
 * to narrow the events received to those for a specific {@link com.tangosol.util.InvocableMap.EntryProcessor}
 * class.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Processor {
    /**
     * The processor class.
     *
     * @return the processor class
     */
    Class<? extends InvocableMap.EntryProcessor> value();

    /**
     * An annotation literal for the {@link Processor}
     * annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<Processor> implements Processor {
        /**
         * The processor class.
         */
        private final Class<? extends InvocableMap.EntryProcessor> processorClass;

        /**
         * Construct {@code Literal} instance.
         *
         * @param clzProcessor the processor class
         */
        private Literal(Class<? extends InvocableMap.EntryProcessor> clzProcessor) {
            this.processorClass = clzProcessor;
        }

        /**
         * Create a {@link Literal}.
         *
         * @param processorClass the processor class
         * @return a {@link Literal}
         * with the specified value
         */
        public static Literal of(Class<? extends InvocableMap.EntryProcessor> processorClass) {
            return new Literal(processorClass);
        }

        /**
         * The processor class.
         *
         * @return the processor class
         */
        public Class<? extends InvocableMap.EntryProcessor> value() {
            return processorClass;
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
            return Objects.equals(processorClass, literal.processorClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), processorClass);
        }
    }
}
