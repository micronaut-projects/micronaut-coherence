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
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that an annotation type is a {@link com.tangosol.util.Filter}
 * binding type.
 *
 * <pre>
 * &#064;Inherited
 * &#064;FilterBinding
 * &#064;Target({TYPE, METHOD, CONSTRUCTOR})
 * &#064;Retention(RUNTIME)
 * public &#064;interface CustomerNameFilter {}
 * </pre>
 *
 * <p>
 * Filter bindings are intermediate annotations that may be used to associate
 * {@link com.tangosol.util.Filter}s with target beans.
 * <p>
 * Filter bindings are used by annotating a {@link io.micronaut.coherence.FilterFactory} bean with the
 * binding type annotations. Wherever the same annotation is used at an
 * injection point that requires a {@link com.tangosol.util.Filter} the
 * corresponding factory's {@link io.micronaut.coherence.FilterFactory#create(java.lang.annotation.Annotation)}
 * method is called to produce a {@link com.tangosol.util.Filter} instance.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface FilterBinding {
}
