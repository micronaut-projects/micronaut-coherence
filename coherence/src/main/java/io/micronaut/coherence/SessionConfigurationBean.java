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

import java.util.ArrayList;
import java.util.List;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.net.options.WithConfiguration;
import com.tangosol.net.options.WithName;
import com.tangosol.net.options.WithScopeName;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

/**
 * @author Jonathan Knight
 * @since 1.0
 */
@EachProperty("coherence.session")
class SessionConfigurationBean implements SessionConfiguration {

    private final String name;

    private String scopeName;

    private String configUri;

    SessionConfigurationBean(@Parameter String name) {
        this.name = "default".equalsIgnoreCase(name) ? Coherence.DEFAULT_NAME : name;
    }

    @Override
    public Session.Option[] getOptions() {
        List<Session.Option> options = new ArrayList<>();

        if ("default".equalsIgnoreCase(name)) {
            options.add(WithName.defaultName());
        } else {
            options.add(WithName.of(name));
        }

        options.add(WithScopeName.of(getScopeName()));

        if (configUri != null) {
            options.add(WithConfiguration.using(configUri));
        }

        return options.toArray(new Session.Option[0]);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScopeName() {
        if (scopeName == null) {
            return Coherence.DEFAULT_NAME.equals(name) ? Coherence.DEFAULT_SCOPE : name;
        } else {
            return scopeName;
        }
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
}
