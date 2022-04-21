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
package io.micronaut.coherence.grpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.oracle.coherence.grpc.proxy.GrpcServerConfiguration;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A bean that collects {@link GrpcServerConfiguration} beans.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
class GrpcServerConfigurer {

    private static final List<GrpcServerConfiguration> CONFIGS = Collections.synchronizedList(new ArrayList<>());

    private final BeanContext beanContext;

    /**
     * Create a {@link GrpcServerConfigurer}.
     *
     * @param beanContext  the Micronaut bean context
     */
    @Inject
    GrpcServerConfigurer(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * Discover any {@link GrpcServerConfiguration} beans on startup.
     *
     * @param event  the startup event
     */
    @EventListener
    void onSampleEvent(StartupEvent event) {
        Collection<GrpcServerConfiguration> beans = beanContext.getBeansOfType(GrpcServerConfiguration.class);
        CONFIGS.addAll(beans);
    }

    /**
     * An implementation of {@link GrpcServerConfiguration} that uses the discovered
     * {@link GrpcServerConfiguration} beans to configure the gRPC servers.
     */
    public static class GrpcServerConfigurationBean implements GrpcServerConfiguration {

        @Override
        public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
            for (GrpcServerConfiguration bean : CONFIGS) {
                bean.configure(serverBuilder, inProcessServerBuilder);
            }
        }
    }
}
