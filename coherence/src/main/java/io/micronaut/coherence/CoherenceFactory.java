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

import javax.inject.Named;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;

import io.micronaut.coherence.events.CoherenceEventListenerProcessor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.inject.InjectionPoint;

/**
 * A factory to provide Coherence resources as Micronaut beans.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
class CoherenceFactory {
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
     * @param configurations    zero or more {@link com.tangosol.net.SessionConfiguration} beans
     * @param configProvider    zero or more {@link com.tangosol.net.SessionConfiguration} beans
     * @param listenerProcessor the CoherenceEventListenerProcessor that discovers event interceptor methods
     *
     * @return the default {@link com.tangosol.net.Coherence} instance used by the Micronaut
     */
    @Singleton
    @Named(Coherence.DEFAULT_NAME)
    Coherence getCoherence(SessionConfiguration[] configurations,
                           SessionConfiguration.Provider[] configProvider,
                           CoherenceEventListenerProcessor listenerProcessor) {

        CoherenceConfiguration cfg = CoherenceConfiguration.builder()
                .withSessions(configurations)
                .withSessionProviders(configProvider)
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
    @Bean
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
    @Singleton
    Cluster getCluster() {
        return CacheFactory.getCluster();
    }
}
