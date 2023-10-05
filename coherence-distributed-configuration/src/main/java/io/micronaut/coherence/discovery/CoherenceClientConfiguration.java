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
package io.micronaut.coherence.discovery;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;

/**
 * The Coherence client configuration.
 */
@ConfigurationProperties(CoherenceClientConfiguration.PREFIX)
@BootstrapContextCompatible
@Requires(property = "coherence.configuration.client.enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class CoherenceClientConfiguration implements Toggleable {

    /**
     * The Coherence configuration client property prefix.
     */
    public static final String PREFIX = "coherence.configuration.client";

    /**
     * Flag indicating whether this configuration client is enabled.
     */
    private boolean enabled;

    /**
     * The name of the {@link com.tangosol.net.Session session} this configuration client will use.
     */
    private String session;

    /**
     * Return the name of the {@link com.tangosol.net.Session session}
     * used by this configuration client.
     *
     * @return the name of the {@link com.tangosol.net.Session session}
     *         used by this configuration client
     */
    public String getSession() {
        return session;
    }

    /**
     * Set the name of the {@link com.tangosol.net.Session session} used by this configuration client.
     *
     * @param session the name of the {@link com.tangosol.net.Session session}
     */
    public void setSession(String session) {
        this.session = session;
    }

    /**
     * Returns {@code true} if distributed configuration is enabled.
     *
     * @return Returns {@code true} if distributed configuration is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables distributed configuration.
     *
     * @param enabled Enable the distributed configuration
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "CoherenceClientConfiguration{" +
                "enabled=" + enabled +
                ", session='" + session + '\'' +
                '}';
    }
}
