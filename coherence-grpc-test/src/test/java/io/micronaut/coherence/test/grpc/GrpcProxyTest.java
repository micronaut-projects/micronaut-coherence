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

import com.oracle.coherence.client.GrpcRemoteSession;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml")
class GrpcProxyTest {

    @Inject
    ApplicationContext context;

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
    public void shouldHaveGrpcSession() {
        Coherence coherence = context.findBean(Coherence.class)
                .orElseThrow(() -> new AssertionError("Could not find Coherence bean"));

        Session session = coherence.getSession("grpc-client");
        assertThat(session, is(instanceOf(GrpcRemoteSession.class)));

        NamedCache<String, String> cache = session.getCache("test");
        cache.put("Key-1", "Value-1");

        assertThat(cache.get("Key-1"), is("Value-1"));
    }
}
