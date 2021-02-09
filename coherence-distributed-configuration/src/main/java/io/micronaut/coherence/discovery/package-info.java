/*
 * Copyright 2017-2021 original authors
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
/**
 * Distributed Configuration client for Oracle Coherence back end.
 *
 * @author Vaso Putica
 * @since 1.0
 */
@Requires(property = ConfigurationClient.ENABLED, value = "true", defaultValue = "false")
@Requires(property = CoherenceClientConfiguration.PREFIX + ".enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Configuration
package io.micronaut.coherence.discovery;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
