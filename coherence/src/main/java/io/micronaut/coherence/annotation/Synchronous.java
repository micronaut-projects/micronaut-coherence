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

/**
 * A qualifier annotation that can be applied to {@link CoherenceEventListener} annotated
 * methods to register them as {@link com.tangosol.util.MapListener#synchronous()} listeners.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Synchronous {
    /**
     * An annotation literal for the {@link Synchronous}
     * annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AnnotationLiteral<Synchronous> implements Synchronous {
        public static final Literal INSTANCE = new Literal();
    }
}
