/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.coherence.tracing;

import io.micronaut.coherence.CoherenceContext;

import io.micronaut.context.ApplicationContext;

import io.micronaut.core.naming.NameUtils;

import io.micronaut.core.naming.conventions.StringConvention;

import java.util.stream.Collectors;

import java.util.Map;

/**
 * A {@link PropertySource} implementation that exposes Micronaut's configuration to
 * the {@code Coherence} {@code OpenTelemetry} integration.
 */
public class PropertySource implements com.tangosol.internal.tracing.PropertySource {
    public Map<String, String> getProperties() {
        ApplicationContext  applicationContext = CoherenceContext.getApplicationContext();
        Map<String, Object> micronautProps     = applicationContext.getProperties("otel", StringConvention.RAW);
        Map<String, String> otelProps          = micronautProps.entrySet().stream().collect(Collectors.toMap(
            e -> "otel." + e.getKey(),
            e -> e.getValue().toString()
        ));

        // we use NameUtils.hyphenate() to be consistent with how
        // Micronaut populates the application name in {@code ApplicationConfiguration}
        // used by micronaut-tracing
        otelProps.put("otel.service.name", NameUtils.hyphenate(
            applicationContext.getProperty("micronaut.application.name", String.class)
                .orElse("micronaut.coherence")));

        return otelProps;
    }
}
