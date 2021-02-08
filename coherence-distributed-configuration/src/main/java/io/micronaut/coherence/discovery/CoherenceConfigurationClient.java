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
import com.tangosol.util.Converter;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.function.Remote;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.context.env.PropertySource;

import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.runtime.ApplicationConfiguration;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@BootstrapContextCompatible
public class CoherenceConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(CoherenceConfigurationClient.class);
    private static final String DEFAULT_APPLICATION = "application";
    /**
     * The name of the map that is used to store configuration properties.
     */
    private static final String CONFIG_MAP_NAME = "sys$config-micronaut";

    private final ApplicationConfiguration applicationConfiguration;
    private final List<Flowable<PropertySource>> propertySources = new ArrayList<>();
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
        ValueExtractor<String, String> keyExtractor = new KeyExtractor<>();
        NamedMap<String, Object> configMap = getMap(CONFIG_MAP_NAME, coherenceClientConfiguration);

        Map<Integer, String> keys = buildKeys(applicationConfiguration, environment);
        for (Map.Entry<Integer, String> entry : keys.entrySet()) {
            final Integer priority = entry.getKey();
            final String propertySource = entry.getValue();
            final String propertySourcePath = propertySource + "/";
            Remote.Predicate<String> pathPredicate = cacheKey -> cacheKey.startsWith(propertySourcePath) &&
                    cacheKey.length() > propertySourcePath.length() &&
                    !cacheKey.substring(propertySourcePath.length()).contains("/");
            Filter<String> filter = Filters.predicate(keyExtractor, pathPredicate);
            Map<String, Object> props = configMap.entrySet(filter)
                    .stream()
                    .collect(Collectors.toMap((Converter<Map.Entry<String, Object>, String>)
                                    cacheEntry -> cacheEntry.getKey().substring(propertySourcePath.length()),
                            Map.Entry::getValue));
            Flowable<PropertySource> propertySourceFlowable = Flowable.just(PropertySource.of(propertySource, props, priority));
            propertySources.add(propertySourceFlowable);
        }
        return Flowable.merge(propertySources);
    }

    /**
     * Returns NamedMap instance.
     *
     * @param configMapName configuration map name
     * @param clientConfig  Coherence client configuration
     * @return configuration map
     */
    protected NamedMap<String, Object> getMap(String configMapName, CoherenceClientConfiguration clientConfig) {
        Session session = buildSession(clientConfig);
        return session.getCache(configMapName);
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
        return optional.get();
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

        ManagedChannelBuilder<?> chanelBuilder = ManagedChannelBuilder.forAddress(host, port);
        if (!coherenceClientConfiguration.isEnableTls()) {
            chanelBuilder.usePlaintext();
        }
        return chanelBuilder.build();
    }

    /**
     * Builds the keys used to get properties.
     *
     * @param applicationConfiguration The application configuration
     * @param environment              environment
     * @return map of precedence values and folders
     */
    protected Map<Integer, String> buildKeys(ApplicationConfiguration applicationConfiguration, Environment environment) {
        Optional<String> configuredApplicationName = applicationConfiguration.getName();
        String applicationName = configuredApplicationName.orElse(null);
        Set<String> environmentNames = environment.getActiveNames();

        Map<Integer, String> configKeys = new HashMap<>();
        int baseOrder = EnvironmentPropertySource.POSITION + 100;
        configKeys.put(++baseOrder, DEFAULT_APPLICATION);
        if (applicationName != null) {
            configKeys.put(++baseOrder, applicationName);
        }

        int envOrder = baseOrder + 50;
        for (String activeName : environmentNames) {
            configKeys.put(++envOrder, DEFAULT_APPLICATION + "/" + activeName);
            if (applicationName != null) {
                configKeys.put(++envOrder, applicationName + "/" + activeName);
            }
        }
        return configKeys;
    }

    @Override
    public String getDescription() {
        return "Reads configuration from Oracle Coherence";
    }
}
