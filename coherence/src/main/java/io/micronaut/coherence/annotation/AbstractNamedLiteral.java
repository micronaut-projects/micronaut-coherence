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
import java.util.Objects;

/**
 * Base class for annotation literals with a single simple name.
 *
 * @param <T> the annotation type
 */
public abstract class AbstractNamedLiteral<T extends Annotation> extends AnnotationLiteral<T> {
    /**
     * The cache name.
     */
    protected final String f_sName;

    /**
     * Construct {@link CacheName.Literal} instance.
     *
     * @param sName the cache name
     */
    protected AbstractNamedLiteral(String sName) {
        f_sName = sName;
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
        final AbstractNamedLiteral<?> that = (AbstractNamedLiteral<?>) o;
        return Objects.equals(f_sName, that.f_sName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), f_sName);
    }
}
