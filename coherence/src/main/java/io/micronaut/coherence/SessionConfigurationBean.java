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
package io.micronaut.coherence;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
@EachProperty(value = "coherence.session", primary = "default")
public class SessionConfigurationBean implements SessionConfiguration.Provider {

    /**
     * The name of the session.
     */
    private String name;

    /**
     * The scope name for the session.
     */
    private String scopeName;

    /**
     * The Coherence cache configuration URI for the session.
     */
    private String configUri;

    /**
     * The priority order to use when starting the {@link com.tangosol.net.Session}.
     * <p>
     * Sessions will be started lowest priority first.
     * @see com.tangosol.net.SessionConfiguration#DEFAULT_PRIORITY
     */
    private int priority = SessionConfiguration.DEFAULT_PRIORITY;

    /**
     * Create a named {@link SessionConfigurationBean}.
     *
     * @param name the name for the session
     */
    protected SessionConfigurationBean(@Parameter String name) {
        setName(name);
    }

    @Override
    public SessionConfiguration getConfiguration() {
        SessionConfiguration.Builder builder = SessionConfiguration
                .builder()
                .named(name)
                .withPriority(priority);

        if (scopeName != null) {
            builder = builder.withScopeName(scopeName);
        }
        if (configUri != null) {
            builder = builder.withConfigUri(configUri);
        }
        return builder.build();
    }

    /**
     * Set the name of this configuration.
     *
     * @param name the name of this configuration
     */
    public void setName(String name) {
        this.name = "default".equalsIgnoreCase(name) ? Coherence.DEFAULT_NAME : name;
    }

    /**
     * Set the scope name for this configuration.
     *
     * @param scopeName the scope name for this configuration
     */
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
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

    /**
     * Set the priority for this configuration.
     * <p>{@link com.tangosol.net.Session Sessions} are started lowest priority first
     * and closed in reverse order.</p>
     *
     * @param priority the priority for this configuration
     * @see com.tangosol.net.SessionConfiguration#getPriority()
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * A marker annotation on a {@link com.tangosol.net.SessionConfiguration} or
     * a {@link com.tangosol.net.SessionConfiguration.Provider} to indicate that
     * it replaces another configuration with the same name.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Replaces {
    }
}
