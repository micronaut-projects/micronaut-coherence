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
package io.micronaut.coherence;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.Name;

import com.tangosol.net.*;

import io.micronaut.coherence.event.CoherenceEventListenerProcessor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.*;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;
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
     * The micronaut bean context.
     */
    private final BeanContext beanContext;

    /**
     * Create a {@link CacheFactory} bean.
     *
     * @param beanContext  the micronaut bean context
     */
    @Inject
    public CoherenceFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * Initialise the Coherence instance on start-up.
     *
     * @param event  the {@link StartupEvent}
     */
    @EventListener
    void onStartupEvent(StartupEvent event) {
        beanContext.createBean(Coherence.class, Qualifiers.byName(Coherence.DEFAULT_NAME));
    }

    /**
     * Creates the default {@link com.tangosol.net.Coherence} instance used by the Micronaut
     * Coherence server.
     * <p>The {@link com.tangosol.net.Coherence} instance is a Micronaut bean and may be injected
     * into application code.</p>
     * <p>The Coherence {@link com.tangosol.net.Session Sessions} created by the Coherence instance
     * will include the default session using the default cache configuration
     * and any other sessions configured from any {@link com.tangosol.net.SessionConfiguration}
     * beans.</p>
     *
     * @param configurations     zero or more {@link com.tangosol.net.SessionConfiguration} beans
     * @param providers          zero or more {@link SessionConfigurationProvider} beans
     * @param lifecycleListeners zero or more {@link com.tangosol.net.Coherence.LifecycleListener} beans
     * @param listenerProcessor  the CoherenceEventListenerProcessor that discovers event interceptor methods
     *
     * @return the default {@link com.tangosol.net.Coherence} instance used by the Micronaut
     */
    @Singleton
    @Bean(preDestroy = "close")
    @Named(Coherence.DEFAULT_NAME)
    Coherence getCoherence(SessionConfiguration[] configurations,
                           SessionConfigurationProvider[] providers,
                           Coherence.LifecycleListener[] lifecycleListeners,
                           CoherenceEventListenerProcessor listenerProcessor) {

        LOG.info("Creating default Coherence instance.");

        // We need to check for an existing instance of Coherence.
        // Even thought his method is annotated as @Singleton it gets called more than once
        Coherence coherence = Coherence.getInstance(Coherence.DEFAULT_NAME);
        if (coherence == null) {
            synchronized (this) {
                coherence = Coherence.getInstance(Coherence.DEFAULT_NAME);
                if (coherence == null) {
                    CoherenceConfiguration cfg = CoherenceConfiguration.builder()
                            .withSessions(CoherenceFactory.collectConfigurations(configurations, providers))
                            .withEventInterceptors(lifecycleListeners)
                            .withEventInterceptors(listenerProcessor.getInterceptors())
                            .build();

                    coherence = Coherence.clusterMember(cfg);
                }
            }
        }

        // start Coherence and wait for it to be started
        coherence.start().join();
        return coherence;
    }

    /**
     * Create a {@link com.tangosol.net.Session} from the qualifiers on the specified
     * injection point.
     *
     * @param injectionPoint the optional injection point that the {@link com.tangosol.net.Session}
     *                       will be injected into
     * @param name           the optional name of the session to return
     *
     * @return a {@link com.tangosol.net.Session}
     */
    @Prototype
    Session getSession(@Nullable InjectionPoint<?> injectionPoint, @Parameter @Nullable String name) {
        String sessionName;
        if (injectionPoint != null) {
            sessionName = injectionPoint.findAnnotation(Name.class)
                    .flatMap(value -> value.getValue(String.class))
                    .orElse(Coherence.DEFAULT_NAME);
        } else if (StringUtils.isNotEmpty(name)) {
            sessionName = name;
        } else {
            sessionName = Coherence.DEFAULT_NAME;
        }

        // ensure that Coherence is started before attempting to get a session
        beanContext.getBean(Coherence.class, Qualifiers.byName(Coherence.DEFAULT_NAME));

        return Coherence.findSession(sessionName)
                .orElseThrow(() -> new IllegalStateException("No Session has been configured with the name " + sessionName));
    }

    /**
     * Return the Coherence {@link Cluster}.
     *
     * @return the Coherence {@link Cluster} (which may or may not be running)
     */
    @Prototype
    Cluster getCluster() {
        return CacheFactory.getCluster();
    }

    /**
     * Collect all of the {@link SessionConfiguration} and {@link SessionConfigurationProvider} beans into
     * a definitive collection of configurations.
     * <p>If two configurations with the same {@link com.tangosol.net.SessionConfiguration#getName() name} are found
     * the first one discovered wins.</p>
     *
     * @param configurations the {@link com.tangosol.net.SessionConfiguration} beans
     * @param providers      the {@link SessionConfigurationProvider} beans
     *
     * @return the definitive collection of configurations to use
     */
    static Collection<SessionConfiguration> collectConfigurations(SessionConfiguration[] configurations,
                                                                  SessionConfigurationProvider[] providers) {

        List<SessionConfiguration> allConfigs = new ArrayList<>(Arrays.asList(configurations));
        Arrays.stream(providers)
                .map(SessionConfigurationProvider::getConfiguration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(allConfigs::add);

        // sort the configurations by priority - highest first
        allConfigs.sort(Comparator.reverseOrder());

        Map<String, SessionConfiguration> configMap = new HashMap<>();
        for (SessionConfiguration cfg : allConfigs) {
            if (cfg != null && cfg.isEnabled()) {
                String name = cfg.getName();
                if (!configMap.containsKey(name)) {
                    configMap.put(name, cfg);
                }
            }
        }

        return configMap.values();
    }

    /**
     * Ensure that Coherence is fully shutdown when the application context is closed.
     * <p>This behaviour can be overridden by setting the configuration property
     * {@code coherence.micronaut.autoCleanup} to {@code false}.</p>
     *
     * @param ctx the application context
     */
    @PreDestroy
    public void shutdownCoherence(ApplicationContext ctx) {
        Boolean cleanUp = ctx.getEnvironment().getProperty("coherence.micronaut.autoCleanup", Boolean.class)
                .orElse(Boolean.TRUE);
        if (cleanUp) {
            LOG.info("Stopping Coherence");
            Coherence.closeAll();
            LOG.info("Stopped Coherence");
        }
    }
}
