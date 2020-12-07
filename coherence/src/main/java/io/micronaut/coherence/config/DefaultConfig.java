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
package io.micronaut.coherence.config;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * @author Jonathan Knight
 * @since 1.0
 */
//@ConfigurationProperties("coherence")
public class DefaultConfig {

    private Micronaut micronaut = new Micronaut();

    /**
     * @return {@code true} if Coherence should be cleaned-up when the
     *         {@link io.micronaut.context.ApplicationContext} is closed
     */
    public boolean isAutoCleanup() {
        return micronaut.isAutoCleanup();
    }

    /**
     * Set the inner micronaut configuration.
     *
     * @param micronaut the inner micronaut configuration
     */
    void setMicronaut(Micronaut micronaut) {
        this.micronaut = micronaut == null ? new Micronaut() : micronaut;
    }

    @ConfigurationProperties("micronaut")
    public static class Micronaut {

        private boolean autoCleanup = true;

        /**
         * @return {@code true} if Coherence should be cleaned-up
         *         when the {@link io.micronaut.context.ApplicationContext} is closed
         */
        boolean isAutoCleanup() {
            return autoCleanup;
        }

        /**
         * Set whether Coherence should be automatically cleaned up.
         *
         * @param autoCleanup  {@code true} if Coherence should be cleaned-up
         *                     when the {@link io.micronaut.context.ApplicationContext} is closed
         */
        void setAutoCleanup(boolean autoCleanup) {
            this.autoCleanup = autoCleanup;
        }
    }
}
