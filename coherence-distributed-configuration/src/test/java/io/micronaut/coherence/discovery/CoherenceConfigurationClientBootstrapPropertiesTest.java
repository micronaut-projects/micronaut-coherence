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
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that Coherence reads its settings from the bootstrap configuration while the client runs in the bootstrap
 * context. No cluster runs, so the startup fails, and Coherence names the cluster it looked for.
 * <p>Coherence reads its operational configuration once per JVM, so this test needs a JVM of its own.</p>
 */
class CoherenceConfigurationClientBootstrapPropertiesTest {

    @AfterEach
    void closeCoherence() {
        Coherence.closeAll();
    }

    @Test
    void coherenceReadsBootstrapConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put("micronaut.application.name", "hello-world");
        props.put("micronaut.config-client.enabled", true);
        props.put("coherence.cluster", "cluster-from-bootstrap-configuration");
        props.put("coherence.sessions.config.type", "client");
        props.put("coherence.configuration.client.enabled", true);
        props.put("coherence.configuration.client.session", "config");

        Exception e = assertThrows(Exception.class, () -> ApplicationContext.builder()
            .bootstrapEnvironment(true)
            .properties(props)
            .start()
            .close());
        String failure = e + " / " + e.getCause();
        assertTrue(failure.contains("cluster-from-bootstrap-configuration"), "looked for another cluster: " + failure);
    }
}
