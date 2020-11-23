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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tangosol.coherence.config.EnvironmentVariableResolver;
import com.tangosol.coherence.config.SystemPropertyResolver;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.Environment;

/**
 * A Coherence {@link com.tangosol.coherence.config.SystemPropertyResolver} and
 * {@link com.tangosol.coherence.config.EnvironmentVariableResolver} that uses
 * the Micronaut {@link io.micronaut.context.env.Environment} to obtain values.
 * <p>This class is annotated as a singleton bean with the
 * {@link io.micronaut.context.annotation.Context} annotation so that its lifecycle
 * is bound to the Micronaut context to ensure it is eagerly instantiated before
 * any Coherence class that might need properties.</p>
 *
 * @author Jonathan Knight  2020.10.21
 */
@Singleton
@Context
public class MicronautSystemPropertyResolver
        implements SystemPropertyResolver, EnvironmentVariableResolver {

    /**
     * The Micronaut {@link io.micronaut.context.env.Environment}.
     */
    private static Environment env;

    /**
     * This constructor is required so that Coherence can discover
     * and instantiate this class using the Java ServiceLoader.
     */
    public MicronautSystemPropertyResolver() {
    }

    /**
     * This constructor will be called by Micronaut to instantiate the
     * singleton bean and set the {@link io.micronaut.context.env.Environment}.
     *
     * @param environment  the Micronaut {@link io.micronaut.context.env.Environment}
     */
    @Inject
    MicronautSystemPropertyResolver(Environment environment) {
        MicronautSystemPropertyResolver.env = environment;
    }

    @Override
    public String getProperty(String s) {
        return MicronautSystemPropertyResolver.env == null
                ? null
                : MicronautSystemPropertyResolver.env.getProperty(s, String.class).orElse(null);
    }

    @Override
    public String getEnv(String s) {
        return MicronautSystemPropertyResolver.env == null
                ? null
                : MicronautSystemPropertyResolver.env.getProperty(s, String.class).orElse(null);
    }
}
