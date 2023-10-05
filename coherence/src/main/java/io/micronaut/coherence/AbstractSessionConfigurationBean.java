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

import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;
import io.micronaut.context.annotation.Parameter;

/**
 * A base {@link SessionConfigurationProvider}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public abstract class AbstractSessionConfigurationBean implements SessionConfigurationProvider {

    /**
     * The name of the session.
     */
    private String name;

    /**
     * The scope name for the session.
     */
    private String scopeName;

    /**
     * The type of this configuration.
     */
    private SessionType type = SessionType.server;

    /**
     * The priority order to use when starting the {@link com.tangosol.net.Session}.
     * <p>
     * Sessions will be started lowest priority first.
     * @see SessionConfiguration#DEFAULT_PRIORITY
     */
    private int priority = SessionConfiguration.DEFAULT_PRIORITY;

    /**
     * Create a named {@link AbstractSessionConfigurationBean}.
     *
     * @param name the name for the session
     */
    protected AbstractSessionConfigurationBean(@Parameter String name) {
        setName(name);
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
     * Return the name of this configuration.
     *
     * @return the name of this configuration
     */
    public String getName() {
        return name;
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
     * Return the scope name for this configuration.
     *
     * @return the scope name for this configuration
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * Set the priority for this configuration.
     * <p>{@link com.tangosol.net.Session Sessions} are started lowest priority first
     * and closed in reverse order.</p>
     *
     * @param priority the priority for this configuration
     * @see SessionConfiguration#getPriority()
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the priority of this configuration.
     *
     * @return the priority of this configuration
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the priority of this configuration.
     *
     * @param type  the type of this configuration
     */
    public void setType(String type) {
        setType(type == null ? SessionType.server : SessionType.valueOf(type));
    }

    /**
     * Set the priority of this configuration.
     *
     * @param type  the type of this configuration
     */
    public void setType(SessionType type) {
        this.type = type;
    }

    /**
     * Returns the type of this configuration.
     *
     * @return the type of this configuration
     */
    public SessionType getType() {
        return type;
    }
}
