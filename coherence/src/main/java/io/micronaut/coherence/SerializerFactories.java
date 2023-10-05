/*
 * Copyright 2017-2023 original authors
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

import com.oracle.coherence.common.base.Classes;

import com.tangosol.io.Serializer;
import com.tangosol.net.Cluster;
import com.tangosol.net.OperationalContext;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * A factory for Coherence {@link com.tangosol.io.Serializer} beans.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
public class SerializerFactories {

    private final OperationalContext context;

    /**
     * Creates a new {@code SerializerFactories} based on the provided
     * {@link Cluster}.
     *
     * @param cluster the {@code Coherence} {@link Cluster}
     */
    @Inject
    public SerializerFactories(Cluster cluster) {
        context = (OperationalContext) cluster;
    }

    /**
     * A factory method to produce the default
     * Java {@link com.tangosol.io.Serializer}.
     *
     * @return the default Java {@link com.tangosol.io.Serializer}
     */
    @Named("java")
    @Singleton
    public Serializer defaultSerializer() {
        return context.getSerializerMap().get("java")
                .createSerializer(Classes.getContextClassLoader());
    }

    /**
     * A factory method to produce the default
     * Java {@link com.tangosol.io.Serializer}.
     *
     * @return the default Java {@link com.tangosol.io.Serializer}
     */
    @Named("pof")
    @Singleton
    public Serializer pofSerializer() {
        return context.getSerializerMap().get("pof")
                .createSerializer(Classes.getContextClassLoader());
    }
}
