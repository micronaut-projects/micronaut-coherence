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

import java.util.Collection;

import com.oracle.coherence.grpc.proxy.GrpcServerConfiguration;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.micronaut.coherence.CoherenceContext;
import io.micronaut.context.ApplicationContext;

/**
 * A {@link GrpcServerConfiguration} implementation that forwards the
 * {@link #configure(io.grpc.ServerBuilder, io.grpc.inprocess.InProcessServerBuilder)}
 * method call to all discovered beans of type {@link GrpcServerConfiguration}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public class GrpcServerConfigurer implements GrpcServerConfiguration {

    /**
     * Public constructor used by Coherence to load this class via the {@link java.util.ServiceLoader}.
     */
    public GrpcServerConfigurer() {
    }

    @Override
    public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
        ApplicationContext context = CoherenceContext.getApplicationContext();
        Collection<GrpcServerConfiguration> beans = context.getBeansOfType(GrpcServerConfiguration.class);
        for (GrpcServerConfiguration bean : beans) {
            bean.configure(serverBuilder, inProcessServerBuilder);
        }
    }
}
