/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.coherence.discovery;

import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads the configuration in the bootstrap context, as an application that uses distributed configuration does.
 */
class CoherenceConfigurationClientBootstrapTest {

    private String clusterName;
    private Session configStore;

    @BeforeEach
    void startConfigServer() {
        // The configuration is held by a cluster member of its own. It is not the default Coherence instance,
        // so the application creates that instance itself, as it does in production.
        Coherence configServer = Coherence.clusterMember(CoherenceConfiguration.builder()
            .named("config-server")
            .withSession(SessionConfiguration.builder()
                .named("config-server")
                .withScopeName(Coherence.DEFAULT_SCOPE)
                .withConfigUri("coherence-cache-config.xml")
                .build())
            .build());
        configServer.start().join();
        clusterName = configServer.getCluster().getClusterName();
        configStore = configServer.getSession("config-server");
        configStore.getMap("application").put("hello", "app hello");
        configStore.getMap("hello-world").put("greeting", "hello-world greeting");
        TestLifecycleListener.STARTED.clear();
    }

    @AfterEach
    void closeCoherence() {
        Coherence.closeAll();
    }

    @Test
    void readsConfigurationInBootstrapContext() {
        try (ApplicationContext app = startApplication()) {
            assertEquals("app hello", app.getProperty("hello", String.class).orElse(null));
            assertEquals("hello-world greeting", app.getProperty("greeting", String.class).orElse(null));
            assertTrue(TestLifecycleListener.STARTED.contains(Coherence.DEFAULT_NAME),
                "the default Coherence instance was not created by the application context");
            assertEquals(1, clientInstances().size());
        }
        assertEquals(List.of(), clientInstances());
    }

    @Test
    void refreshKeepsApplicationCoherenceRunning() {
        try (ApplicationContext app = startApplication()) {
            Coherence coherence = Coherence.getInstance(Coherence.DEFAULT_NAME);
            NamedMap<String, String> appData = app.createBean(Session.class).getMap("app-data");
            NamedMap<String, String> appConfigData = app.createBean(Session.class, "config").getMap("app-data");
            appData.put("key", "before");
            appConfigData.put("key", "before");

            configStore.getMap("application").put("hello", "app hello 2");
            app.getEnvironment().refresh();

            assertEquals("app hello 2", app.getProperty("hello", String.class).orElse(null));
            assertSame(coherence, Coherence.getInstance(Coherence.DEFAULT_NAME));
            assertTrue(coherence.isStarted());
            appData.put("key", "after");
            assertEquals("after", appData.get("key"));
            appConfigData.put("key", "after");
            assertEquals("after", appConfigData.get("key"));
            assertEquals(1, clientInstances().size());
        }
    }

    private ApplicationContext startApplication() {
        Map<String, Object> props = new HashMap<>();
        props.put("spec.name", "CoherenceConfigurationClientBootstrapTest");
        props.put("micronaut.application.name", "hello-world");
        props.put("micronaut.config-client.enabled", true);
        props.put("coherence.cluster", clusterName);
        props.put("coherence.sessions.default.type", "server");
        props.put("coherence.sessions.default.config", "coherence-cache-config.xml");
        props.put("coherence.sessions.config.type", "client");
        props.put("coherence.configuration.client.enabled", true);
        props.put("coherence.configuration.client.session", "config");
        return ApplicationContext.builder()
            .bootstrapEnvironment(true)
            .properties(props)
            .start();
    }

    private static List<String> clientInstances() {
        return Coherence.getInstances().stream()
            .map(Coherence::getName)
            .filter(name -> name.startsWith("micronaut-configuration-client-"))
            .toList();
    }
}
