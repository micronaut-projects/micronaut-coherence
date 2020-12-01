/*
 * Copyright 2017-2020 original authors
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.context.annotation.Executable;

/**
 * <p>An {@link Executable} advice annotation that allows listening for Coherence events.</p>
 * <p>The method will ultimately be wrapped in either an {@link com.tangosol.net.events.EventInterceptor}
 * or a {@link com.tangosol.util.MapListener}.
 * Various qualifier annotations can also be applied to further qualify the types of events and the target event source
 * for a specific listener method. Listener methods can have any name but must take a single parameter that extends either
 * {@link com.tangosol.net.events.Event} or {@link com.tangosol.util.MapEvent} and return {@code void}.</p>
 *
 * <p>For example:</p>
 * <p>The following method will receive a {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent} event every
 * time a map or cache is created or destroyed.</p>
 *
 * <pre><code>
 *  {@literal @}CoherenceEventListener
 *   public void onEvent(CacheLifecycleEvent event) {
 *   }
 * </code></pre>
 *
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Executable(processOnStartup = true)
public @interface CoherenceEventListener {
}
