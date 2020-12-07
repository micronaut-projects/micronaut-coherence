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
import io.micronaut.coherence.CoherenceContext;

/**
 * An implementation of a {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} that
 * locates a {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} bean to proxy the
 * {@link com.oracle.coherence.grpc.proxy.GrpcServerBuilderProvider} call to.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public class GrpcServerBuilder implements GrpcServerBuilderProvider {
    @Override
    public ServerBuilder<?> getServerBuilder(int port) {
        return CoherenceContext.getApplicationContext().findBean(ServerBuilder.class)
                .orElse(INSTANCE.getServerBuilder(port));
    }

    @Override
    public InProcessServerBuilder getInProcessServerBuilder(String name) {
        return INSTANCE.getInProcessServerBuilder(name);
    }
}
