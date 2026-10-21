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

import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import io.micronaut.coherence.SessionConfigurationProvider;
import io.micronaut.coherence.config.MicronautSystemPropertyResolver;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ConfigurationClient} that works with Coherence as a config source.
 * <p>The client usually runs in the bootstrap context, before the application context exists. It reads the
 * configuration through a Coherence instance of its own, created for the session named by
 * {@code coherence.configuration.client.session}. It does not use the default Coherence instance, which the
 * application context creates with the application's sessions, listeners and interceptors. The values are copied
 * into the property sources.</p>
 * <p>The client does not close its Coherence instance: a session with the same scope and configuration in the
 * application shares the same services, and closing it would close them too. The instance is reused by later
 * clients for the same session, and closed with the other Coherence instances when the application stops.</p>
 */
@Singleton
@BootstrapContextCompatible
@Requires(beans = CoherenceClientConfiguration.class)
public class CoherenceConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(CoherenceConfigurationClient.class);
    private static final String DEFAULT_APPLICATION = "application";
    private static final String DEFAULT_SESSION = "default";
    private static final String INSTANCE_NAME_PREFIX = "micronaut-configuration-client-";

    private final ApplicationConfiguration applicationConfiguration;
    private final CoherenceClientConfiguration coherenceClientConfiguration;
    private final BeanContext beanContext;

    /**
     * Construct a new {@code CoherenceConfigurationClient}.
     *
     * @param applicationConfiguration the {@link ApplicationConfiguration application configuration}
     * @param coherenceClientConfiguration the {@link CoherenceClientConfiguration client configuration}
     * @param beanContext the {@link BeanContext bean context}
     */
    @SuppressWarnings("MnInjectionPoints")
    public CoherenceConfigurationClient(ApplicationConfiguration applicationConfiguration,
                                        CoherenceClientConfiguration coherenceClientConfiguration,
                                        BeanContext beanContext) {
        this.applicationConfiguration = applicationConfiguration;
        this.coherenceClientConfiguration = coherenceClientConfiguration;
        this.beanContext = beanContext;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Coherence Client configuration: {}" , coherenceClientConfiguration);
        }
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        if (!coherenceClientConfiguration.isEnabled()) {
            return Flux.empty();
        }
        // in the bootstrap context, Coherence reads its settings from the bootstrap configuration
        MicronautSystemPropertyResolver.useUntilBound(environment);
        Session configSession = getSession();

        Map<Integer, String> keys = buildSourceNames(applicationConfiguration, environment);
        List<PropertySource> propertySources = new ArrayList<>(keys.size());
        for (Map.Entry<Integer, String> entry : keys.entrySet()) {
            final Integer priority = entry.getKey();
            final String propertySource = entry.getValue();
            NamedMap<String, Object> configMap = configSession.getMap(propertySource);
            propertySources.add(PropertySource.of(propertySource, new HashMap<>(configMap), priority));
        }
        return Flux.fromIterable(propertySources);
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

    @Override
    public String getDescription() {
        return "Reads configuration from Oracle Coherence";
    }

    private Session getSession() {
        SessionConfiguration sessionConfiguration = sessionConfiguration();
        String instanceName = INSTANCE_NAME_PREFIX + sessionConfiguration.getName();
        Coherence coherence;
        synchronized (CoherenceConfigurationClient.class) {
            coherence = Coherence.getInstance(instanceName);
            if (coherence == null || coherence.isClosed()) {
                coherence = Coherence.create(CoherenceConfiguration.builder()
                        .named(instanceName)
                        .withSession(sessionConfiguration)
                        .build(), sessionConfiguration.getMode().orElse(Coherence.Mode.ClusterMember));
            }
        }
        coherence.start().join();
        return coherence.getSession(sessionConfiguration.getName());
    }

    private SessionConfiguration sessionConfiguration() {
        String name = coherenceClientConfiguration.getSession();
        String sessionName = StringUtils.isNotEmpty(name) ? name : DEFAULT_SESSION;
        Optional<SessionConfiguration> configuration = beanContext
                .findBean(SessionConfigurationProvider.class, Qualifiers.byName(sessionName))
                .flatMap(SessionConfigurationProvider::getConfiguration);
        if (configuration.isPresent()) {
            return configuration.get();
        }
        if (DEFAULT_SESSION.equals(sessionName)) {
            return SessionConfiguration.defaultSession();
        }
        throw new ConfigurationException("No Session has been configured with the name " + sessionName);
    }
}
