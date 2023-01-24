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

import com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of a {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} that
 * locates a {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} bean to proxy the
 * {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} call to.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
@SuppressWarnings("rawtypes")
class GrpcServerBuilder {

    private static final List<ServerBuilder> BUILDERS = Collections.synchronizedList(new ArrayList<>());

    private final BeanContext beanContext;

    /**
     * Create a {@link GrpcServerBuilder}.
     *
     * @param beanContext  the Micronaut bean context
     */
    @Inject
    GrpcServerBuilder(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    /**
     * Discover any {@link ServerBuilder} beans on startup.
     *
     * @param event  the startup event
     */
    @EventListener
    void onSampleEvent(StartupEvent event) {
        Collection<ServerBuilder> beans = beanContext.getBeansOfType(ServerBuilder.class);
        BUILDERS.addAll(beans);
    }

    /**
     * An implementation of {@link GrpcServerBuilderProvider} that uses the discovered
     * {@link GrpcServerBuilderProvider} beans to supply the gRPC {@link ServerBuilder}.
     */
    public static class GrpcServerBuilderProviderBean implements GrpcServerBuilderProvider {

        @Override
        public ServerBuilder<?> getServerBuilder(int port) {
            if (BUILDERS.isEmpty()) {
                return INSTANCE.getServerBuilder(port);
            } else {
                return BUILDERS.get(0);
            }
        }

        @Override
        public InProcessServerBuilder getInProcessServerBuilder(String name) {
            return INSTANCE.getInProcessServerBuilder(name);
        }
    }
}
