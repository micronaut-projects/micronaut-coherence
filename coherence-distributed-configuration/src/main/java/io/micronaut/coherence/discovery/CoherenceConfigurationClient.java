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
package io.micronaut.coherence.discovery;


import com.oracle.coherence.client.GrpcSessionConfiguration;

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.runtime.ApplicationConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Singleton;

import org.reactivestreams.Publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

/**
 * A {@link ConfigurationClient} that works with Coherence as a config source.
 */
@Singleton
@BootstrapContextCompatible
@Requires(beans = CoherenceClientConfiguration.class)
public class CoherenceConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(CoherenceConfigurationClient.class);
    private static final String DEFAULT_APPLICATION = "application";

    private final ApplicationConfiguration applicationConfiguration;
    private final List<Flux<PropertySource>> propertySources = new ArrayList<>();
    private final CoherenceClientConfiguration coherenceClientConfiguration;

    public CoherenceConfigurationClient(ApplicationConfiguration applicationConfiguration,
                                        CoherenceClientConfiguration coherenceClientConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        this.coherenceClientConfiguration = coherenceClientConfiguration;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Coherence Client configuration: {}" , coherenceClientConfiguration);
        }
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        if (!coherenceClientConfiguration.isEnabled()) {
            return Flux.empty();
        }
        Session session = buildSession(coherenceClientConfiguration);

        Map<Integer, String> keys = buildSourceNames(applicationConfiguration, environment);
        for (Map.Entry<Integer, String> entry : keys.entrySet()) {
            final Integer priority = entry.getKey();
            final String propertySource = entry.getValue();
            NamedMap<String, Object> configMap = session.getMap(propertySource);
            Flux<PropertySource> propertySourceFlux = Flux.just(PropertySource.of(propertySource, configMap, priority));
            propertySources.add(propertySourceFlux);
        }
        return Flux.merge(propertySources);
    }

    /**
     * Build a map of config source names.
     *
     * @param applicationConfiguration  the application configuration
     * @param environment               the current environment
     *
     * @return a map of config source names
     */
    protected Map<Integer, String> buildSourceNames(ApplicationConfiguration applicationConfiguration, Environment environment) {
        Optional<String> configuredApplicationName = applicationConfiguration.getName();
        String applicationName = configuredApplicationName.orElse(null);
        Set<String> environmentNames = environment.getActiveNames();

        Map<Integer, String> configKeys = new HashMap<>();
        int baseOrder = EnvironmentPropertySource.POSITION + 100;
        configKeys.put(++baseOrder, DEFAULT_APPLICATION);
        if (applicationName != null) {
            configKeys.put(++baseOrder,  applicationName);
        }

        int envOrder = baseOrder + 50;
        for (String activeName : environmentNames) {
            configKeys.put(++envOrder, DEFAULT_APPLICATION + "-" + activeName);
            if (applicationName != null) {
                configKeys.put(++envOrder,  applicationName + "-" + activeName);
            }
        }
        return configKeys;
    }

    /**
     * Builds Coherence session.
     *
     * @param coherenceClientConfiguration configuration
     * @return Coherence session
     */
    protected Session buildSession(CoherenceClientConfiguration coherenceClientConfiguration) {
        Channel channel = buildChannel(coherenceClientConfiguration);

        GrpcSessionConfiguration.Builder builder = GrpcSessionConfiguration.builder(channel);
        GrpcSessionConfiguration grpcSessionConfiguration = builder.build();

        Optional<Session> optional = Session.create(grpcSessionConfiguration);
        return optional.orElseThrow(() -> new IllegalStateException("Unable to create session"));
    }

    /**
     * Builds gRPC channel.
     *
     * @param coherenceClientConfiguration configuration
     * @return gRPC channel
     */
    protected Channel buildChannel(CoherenceClientConfiguration coherenceClientConfiguration) {
        String host = coherenceClientConfiguration.getHost();
        int port = coherenceClientConfiguration.getPort();

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);
        if (!coherenceClientConfiguration.isEnableTls()) {
            channelBuilder.usePlaintext();
        }
        return channelBuilder.build();
    }

    @Override
    public String getDescription() {
        return "Reads configuration from Oracle Coherence";
    }
}
