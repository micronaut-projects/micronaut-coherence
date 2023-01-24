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
package io.micronaut.coherence.test.grpc;

import com.oracle.coherence.grpc.proxy.GrpcServerConfiguration;
import com.tangosol.net.Coherence;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "GrpcServerConfigurerTest")
class GrpcServerConfigurerTest {

    private static final CountDownLatch latch = new CountDownLatch(2);

    @Inject
    ApplicationContext context;

    // We need to inject Coherence to trigger the bootstrap
    @Inject
    Coherence coherence;

    @Test
    public void shouldHaveCalledAllServerConfigurationBeans() throws InterruptedException {
        latch.await(5, TimeUnit.SECONDS);
        context.findBean(ServerConfigurationOne.class)
                .orElseThrow(() -> new AssertionError("Could not find ServerConfigurationOne bean"));

        context.findBean(ServerConfigurationTwo.class)
                .orElseThrow(() -> new AssertionError("Could not find ServerConfigurationTwo bean"));
    }

    @Singleton
    @Requires(env = "GrpcServerConfigurerTest")
    static class ServerConfigurationOne implements GrpcServerConfiguration {
        @Override
        public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
            latch.countDown();
        }
    }

    @Singleton
    @Requires(env = "GrpcServerConfigurerTest")
    static class ServerConfigurationTwo implements GrpcServerConfiguration {
        @Override
        public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
            latch.countDown();
        }
    }
}
