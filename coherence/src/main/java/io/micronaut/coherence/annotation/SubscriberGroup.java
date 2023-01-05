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
 * A qualifier annotation used when injecting {@link com.tangosol.net.topic.Subscriber}
 * to a {@link com.tangosol.net.topic.NamedTopic} to indicate the name of the
 * subscriber group that the subscriber should belong to.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscriberGroup {
    /**
     * The name of the subscriber group.
     *
     * @return the name of the subscriber group
     */
    String value();


    /**
     * An annotation literal for the {@link SubscriberGroup} annotation.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    final class Literal extends AbstractNamedLiteral<SubscriberGroup> implements SubscriberGroup {
        /**
         * Construct {@code Literal} instance.
         *
         * @param sName the name of the subscriber group
         */
        private Literal(String sName) {
            super(sName);
        }

        /**
         * Create a {@link Literal}.
         *
         * @param sName the name of the subscriber group
         * @return a {@link Literal} with the specified value
         */
        public static Literal of(String sName) {
            return new Literal(sName);
        }

        /**
         * The name of the subscriber group.
         *
         * @return the name of the subscriber group
         */
        public String value() {
            return f_sName;
        }
    }
}
