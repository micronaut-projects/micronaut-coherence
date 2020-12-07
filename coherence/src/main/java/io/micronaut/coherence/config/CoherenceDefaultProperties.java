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

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.MapPropertySource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * A bean that provides Coherence property defaults via a
 * {@link io.micronaut.context.env.PropertySource}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Context
@Singleton
class CoherenceDefaultProperties {

    /**
     * Default service name prefix. Defaults recursively to ${code coherence.role}.
     */
    private static final String SERVICE_NAME_PREFIX = "coherence.service.prefix";

    /**
     * The name of the config property for logging destination.
     */
    private static final String LOG_DESTINATION = "coherence.log";

    /**
     * The name of the config property for logger name.
     */
    private static final String LOG_LOGGER_NAME = "coherence.log.logger";

    /**
     * The name of the config property for message format.
     */
    private static final String LOG_MESSAGE_FORMAT = "coherence.log.format";

    @Inject
    CoherenceDefaultProperties(ApplicationContext context) {
        context.getEnvironment().addPropertySource(new CoherenceDefaultPropertySource());
    }

    private static Map<String, Object> createConfig() {
        Map<String, Object> map = new HashMap<>();

        // service defaults
        map.put(SERVICE_NAME_PREFIX, "${coherence.role}");

        // logging defaults
        map.put(LOG_DESTINATION, "slf4j");
        map.put(LOG_LOGGER_NAME, "coherence");
        map.put(LOG_MESSAGE_FORMAT, "(thread={thread}, member={member}, up={uptime}): {text}");

        return map;
    }

    /**
     * An implementation of a {@link io.micronaut.context.env.PropertySource}
     * to provide the default properties.
     */
    static class CoherenceDefaultPropertySource extends MapPropertySource {

        CoherenceDefaultPropertySource() {
            super("coherence.defaults", createConfig());
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }
    }
}
