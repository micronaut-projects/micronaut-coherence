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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.grpc.proxy.GrpcServerConfiguration;

import com.tangosol.net.Coherence;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "GrpcServerConfigurerTest")
class GrpcServerConfigurerTest {

    @Inject
    ApplicationContext context;

    // We need to inject Coherence to trigger the bootstrap
    @Inject
    Coherence coherence;

    @AfterAll
    static void cleanup() {
        // Close the gRPC session for faster test clean-up
        // ToDo: Should be able to remove this when start/stop ordering is in Coherence
        Coherence.findSession("grpc-client")
                .ifPresent(s -> {
                    try {
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void shouldHaveCalledAllServerConfigurationBeans() {
        ServerConfigurationOne cfgOne = context.findBean(ServerConfigurationOne.class)
                .orElseThrow(() -> new AssertionError("Could not find ServerConfigurationOne bean"));

        ServerConfigurationTwo cfgTwo = context.findBean(ServerConfigurationTwo.class)
                .orElseThrow(() -> new AssertionError("Could not find ServerConfigurationTwo bean"));

        assertThat(cfgOne.isExecuted(), is(true));
        assertThat(cfgTwo.isExecuted(), is(true));
    }

    @Singleton
    @Requires(env = "GrpcServerConfigurerTest")
    static class ServerConfigurationOne implements GrpcServerConfiguration {

        private boolean executed;

        @Override
        public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }

    @Singleton
    @Requires(env = "GrpcServerConfigurerTest")
    static class ServerConfigurationTwo implements GrpcServerConfiguration {

        private boolean executed;

        @Override
        public void configure(ServerBuilder<?> serverBuilder, InProcessServerBuilder inProcessServerBuilder) {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }
}
