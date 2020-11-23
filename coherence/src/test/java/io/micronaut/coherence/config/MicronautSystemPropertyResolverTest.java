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
package io.micronaut.coherence.config;

import com.tangosol.coherence.config.Config;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test that Coherence configuration properties can be provided by
 * Micronaut configuration via the {@link MicronautSystemPropertyResolver}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@MicronautTest(startApplication = false, propertySources = "classpath:micronaut-system-property-resolver-test.yaml")
class MicronautSystemPropertyResolverTest {
    @Test
    void shouldGetRoleFromMicronaut() {
        String role = Config.getProperty("coherence.role");
        assertThat(role, is("property-test"));
    }
}
