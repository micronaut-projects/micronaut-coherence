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
package io.micronaut.coherence.httpsession;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;
import io.micronaut.session.http.HttpSessionConfiguration;

import java.util.Optional;


/**
 * A {@link HttpSessionConfiguration} that uses Coherence to store http sessions.
 */
@ConfigurationProperties("coherence")
public class CoherenceHttpSessionConfiguration extends HttpSessionConfiguration implements Toggleable {
    private String cacheName;

    public CoherenceHttpSessionConfiguration() {
    }

    /**
     * Get HTTP session cache name.
     *
     * @return session cache name
     */
    public Optional<String> getCacheName() {
        return Optional.ofNullable(cacheName);
    }

    /**
     * Set HTTP session cache name.
     *
     * @param cacheName session cache name
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }
}
