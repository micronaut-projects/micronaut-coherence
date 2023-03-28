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
package io.micronaut.coherence;

import com.oracle.coherence.io.json.JsonSerializer;

import com.tangosol.io.Serializer;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * A factory that provides a {@link com.oracle.coherence.io.json.JsonSerializer} beans.
 * <p>This factory is only enabled when the {@code coherence-json} module is on the classpath</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
@Requires(classes = JsonSerializer.class)
public class JsonSerializerFactory {
    /**
     * A factory method to produce the default
     * Java {@link com.tangosol.io.Serializer}.
     *
     * @return the default Java {@link com.tangosol.io.Serializer}
     */
    @Named("json")
    @Singleton
    public Serializer defaultSerializer() {
        return new JsonSerializer();
    }
}
