/*
 * Copyright 2017-2022 original authors
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

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * Base class for array-based annotation literals.
 *
 * @param <T> the annotation type
 */
public abstract class AbstractArrayLiteral<T extends Annotation> extends AnnotationLiteral<T> {

    /**
     * The values for this annotation.
     */
    protected Object[] array;

    /**
     * Constructs a new array-based annotation literal.
     *
     * @param array the values.
     */
    protected AbstractArrayLiteral(final Object[] array) {
        this.array = array;
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
        final AbstractArrayLiteral<?> literal = (AbstractArrayLiteral<?>) o;
        return Arrays.equals(array, literal.array);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }
}
