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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;
import com.tangosol.net.*;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.inject.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory to provide Coherence resources as Micronaut beans.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
class CoherenceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CoherenceFactory.class);

    /**
     * Creates the default {@link com.tangosol.net.Coherence} instance used by the Micronaut
     * Coherence server.
     * <p>The {@link com.tangosol.net.Coherence} instance is a Micronaut bean and may be injected
     * into application code.</p>
     * <p>The Coherence {@link com.tangosol.net.Session Sessions} created by the Coherence instance
     * will include the default session using the default cache configuration
     * and any other sessions configured from any {@link com.tangosol.net.SessionConfiguration}
     * beans or {@link com.tangosol.net.SessionConfiguration.Provider} beans.</p>
     *
     * @param configurations     zero or more {@link com.tangosol.net.SessionConfiguration} beans
     * @param providers          zero or more {@link com.tangosol.net.SessionConfiguration} beans
     * @param lifecycleListeners zero or more {@link com.tangosol.net.Coherence.LifecycleListener} beans
     * @param listenerProcessor  the CoherenceEventListenerProcessor that discovers event interceptor methods
     *
     * @return the default {@link com.tangosol.net.Coherence} instance used by the Micronaut
     */
    @Singleton
    @Bean(preDestroy = "close")
    @Named(Coherence.DEFAULT_NAME)
    Coherence getCoherence(SessionConfiguration[] configurations,
                           SessionConfiguration.Provider[] providers,
                           Coherence.LifecycleListener[] lifecycleListeners,
                           CoherenceEventListenerProcessor listenerProcessor) {

        LOG.info("Creating default Coherence instance.");

        CoherenceConfiguration cfg = CoherenceConfiguration.builder()
                .withSessions(CoherenceFactory.collectConfigurations(configurations, providers))
                .withEventInterceptors(lifecycleListeners)
                .withEventInterceptors(listenerProcessor.getInterceptors())
                .build();

        return Coherence.builder(cfg).build();
    }

    /**
     * Create a {@link com.tangosol.net.Session} from the qualifiers on the specified
     * injection point.
     *
     * @param injectionPoint the injection point that the {@link com.tangosol.net.Session}
     *                       will be injected into
     * @return a {@link com.tangosol.net.Session}
     */
    @Prototype
    @Named("Name")
    Session getSession(InjectionPoint<?> injectionPoint) {
        String sName = injectionPoint.findAnnotation(Name.class)
                .flatMap(value -> value.getValue(String.class))
                .orElse(Coherence.DEFAULT_NAME);

        return Coherence.findSession(sName)
                .orElseThrow(() -> new IllegalStateException("No Session has been configured with the name " + sName));
    }

    /**
     * Return the Coherence {@link Cluster}.
     *
     * @return the Coherence {@link Cluster} (which may or may not be running)
     */
    @Prototype
//    @Bean(preDestroy = "shutdown")
    Cluster getCluster() {
        return CacheFactory.getCluster();
    }

    /**
     * Collect all of the {@link SessionConfiguration} and {@link SessionConfiguration.Provider} beans into
     * a definitive collection of configurations.
     * <p>If two configurations with the same {@link com.tangosol.net.SessionConfiguration#getName() name} are found
     * the first one discovered wins unless the subsequent beans are annotated with the
     * {@link io.micronaut.coherence.SessionConfigurationBean.Replaces} annotation</p>
     *
     * @param configurations the {@link com.tangosol.net.SessionConfiguration} beans
     * @param providers      the {@link com.tangosol.net.SessionConfiguration.Provider} beans
     *
     * @return the definitive collection of configurations to use
     */
    static Collection<SessionConfiguration> collectConfigurations(SessionConfiguration[] configurations,
                                                                  SessionConfiguration.Provider[] providers) {

        Map<String, SessionConfiguration> configMap = new HashMap<>();
        for (SessionConfiguration cfg : configurations) {
            if (cfg != null && cfg.isEnabled()) {
                String name = cfg.getName();
                if (!configMap.containsKey(name) || cfg.getClass().isAnnotationPresent(SessionConfigurationBean.Replaces.class)) {
                    configMap.put(name, cfg);
                }
            }
        }

        for (SessionConfiguration.Provider provider : providers) {
            SessionConfiguration cfg = provider.getConfiguration();
            if (cfg != null && cfg.isEnabled()) {
                String name = cfg.getName();
                if (!configMap.containsKey(name) || provider.getClass().isAnnotationPresent(SessionConfigurationBean.Replaces.class)) {
                    configMap.put(name, cfg);
                }
            }
        }

        return configMap.values();
    }
//
//    @CoherenceEventListener
//    @Synchronous
//    void cleanup(@Disposing LifecycleEvent event) {
//        ConfigurableCacheFactory ccf = event.getConfigurableCacheFactory();
//        CacheFactory.getCacheFactoryBuilder().release(ccf);
//    }
}
