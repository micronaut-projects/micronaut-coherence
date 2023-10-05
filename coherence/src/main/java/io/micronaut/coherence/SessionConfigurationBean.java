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
package io.micronaut.coherence;

import java.util.Optional;

import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

/**
 * A {@link SessionConfiguration} bean that will be created for
 * each named session in the application configuration properties.
 * <p>Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.</p>
 * <p>The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@EachProperty(value = "coherence.sessions", primary = "default")
public class SessionConfigurationBean extends AbstractSessionConfigurationBean {

    /**
     * The Coherence cache configuration URI for the session.
     */
    private String configUri;

    /**
     * Create a named {@link SessionConfigurationBean}.
     *
     * @param name the name for the session
     */
    protected SessionConfigurationBean(@Parameter String name) {
        super(name);
    }

    @Override
    public Optional<SessionConfiguration> getConfiguration() {
        SessionConfiguration.Builder builder = SessionConfiguration
                .builder()
                .named(getName())
                .withPriority(getPriority());

        SessionType type = getType();
        switch (type) {
            case client -> builder.withMode(Coherence.Mode.Client);
            case grpc   -> builder.withMode(Coherence.Mode.Grpc);
            default     -> builder.withMode(Coherence.Mode.ClusterMember);
        }

        String scopeName = getScopeName();
        if (scopeName != null) {
            builder = builder.withScopeName(scopeName);
        }

        if (configUri != null) {
            builder = builder.withConfigUri(configUri);
        }

        return Optional.of(builder.build());
    }

    /**
     * Get the Coherence cache configuration URI.
     *
     * @return the Coherence cache configuration URI
     */
    public String getConfig() {
        return configUri;
    }

    /**
     * Set the Coherence cache configuration URI.
     *
     * @param configUri the Coherence cache configuration URI
     */
    public void setConfig(String configUri) {
        this.configUri = configUri;
    }
}
