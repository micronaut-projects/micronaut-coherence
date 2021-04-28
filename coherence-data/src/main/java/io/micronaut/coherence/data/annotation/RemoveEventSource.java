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
package io.micronaut.coherence.data.annotation;

import io.micronaut.aop.Around;
import io.micronaut.core.annotation.Internal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@code Around} advice to intercept methods what which to trigger Micronaut Data {@code remove} entity event.
 *
 * @see io.micronaut.data.annotation.event.PreRemove
 * @see io.micronaut.data.annotation.event.PostRemove
 */
@Internal
@Retention(RUNTIME)
@Target({METHOD})
@Around
public @interface RemoveEventSource {
}
