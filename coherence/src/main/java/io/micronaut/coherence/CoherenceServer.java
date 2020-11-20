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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tangosol.net.Coherence;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.EmbeddedApplication;

/**
 * An {@link EmbeddedApplication} that will start the default
 * {@link com.tangosol.net.Coherence} instance.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
public class CoherenceServer
        implements EmbeddedApplication<CoherenceServer> {

    /**
     * The bean context.
     */
    private final ApplicationContext ctx;

    /**
     * The Coherence application configuration.
     */
    private final DefaultCoherenceConfiguration config;

    /**
     * The {@link com.tangosol.net.Coherence} instance to run.
     */
    private final Coherence coherence;

    /**
     * Create a {@link CoherenceServer}.
     *
     * @param ctx       the Micronaut {@link io.micronaut.context.ApplicationContext}
     * @param config    the server configuration
     * @param coherence the {@link com.tangosol.net.Coherence} instance to run
     */
    @Inject
    public CoherenceServer(ApplicationContext ctx, DefaultCoherenceConfiguration config, Coherence coherence) {
        this.ctx = ctx;
        this.config = config;
        this.coherence = coherence;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
//        return config.getApplicationConfiguration();
        return null;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Override
    public boolean isRunning() {
        return coherence != null && coherence.isStarted();
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @NonNull
    @Override
    public synchronized CoherenceServer start() {
        // ToDo: blocking until Coherence has started - do we need to??
        coherence.start().join();
        return this;
    }

    @NonNull
    @Override
    public synchronized CoherenceServer stop() {
        if (coherence != null) {
            coherence.close();
        }
        Coherence.closeAll();
        return this;
    }
}
