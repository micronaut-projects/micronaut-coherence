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
package io.micronaut.coherence.client;

import java.util.Optional;

import com.oracle.coherence.client.GrpcSessionConfiguration;

import com.tangosol.io.Serializer;
import com.tangosol.net.SessionConfiguration;

import io.grpc.Channel;
import io.micronaut.coherence.SessionConfigurationBean;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.inject.qualifiers.Qualifiers;

/**
 * A {@link com.tangosol.net.SessionConfiguration.Provider} bean that will be
 * created for each named session in the application configuration properties.
 *
 * <p>This configuration bean specifically produces {@link GrpcSessionConfiguration}
 * beans <i>only</i> if the configuration has a {@code channelName} property. The
 * {@code channelName} refers to the name of a {@link io.grpc.Channel} bean.</p>
 *
 * <p>Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.</p>
 *
 * <p>The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@EachProperty(value = "coherence.session")
@SessionConfigurationBean.Replaces
class GrpcSessionConfigurationBean implements SessionConfiguration.Provider {

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext ctx;

    /**
     * The name of the {@link com.tangosol.net.Session}.
     */
    private final String name;

    /**
     * The name of the gRPC {@link Channel} bean.
     */
    private String channelName;

    /**
     * The nam eof the {@link com.tangosol.io.Serializer} bean.
     */
    private String serializer;

    /**
     * {@code true} to enable distributed tracing for the gRPC methods.
     */
    private boolean tracingEnabled;

    /**
     * The priority order to use when starting the {@link com.tangosol.net.Session}.
     * <p>
     * Sessions will be started lowest priority first.
     * @see com.tangosol.net.SessionConfiguration#DEFAULT_PRIORITY
     */
    private int priority = SessionConfiguration.DEFAULT_PRIORITY;

    /**
     * Create a named {@link io.micronaut.coherence.client.GrpcSessionConfigurationBean}.
     *
     * @param name the name for the session
     * @param ctx  the Micronaut bean context
     */
    GrpcSessionConfigurationBean(@Parameter String name, ApplicationContext ctx) {
        this.name = name;
        this.ctx = ctx;
    }

    @Override
    public SessionConfiguration getConfiguration() {
        if (channelName == null || channelName.trim().isEmpty()) {
            // there is no channel name so this is not a gRPC configuration.
            return null;
        }

        Channel channel = ctx.findBean(Channel.class, Qualifiers.byName(channelName))
                .orElseThrow(() -> new ConfigurationException("No bean of type io.grpc.Channel is configured with name " + channelName));

        GrpcSessionConfiguration.Builder builder = GrpcSessionConfiguration.builder(channel)
                .named(name)
                .withTracing(tracingEnabled)
                .withPriority(priority);

        if (serializer != null && serializer.trim().isEmpty()) {
            Optional<Serializer> optional = ctx.findBean(Serializer.class, Qualifiers.byName(serializer));
            if (optional.isPresent()) {
                builder.withSerializer(optional.get(), serializer);
            } else {
                builder.withSerializerFormat(serializer);
            }
        }

        return builder.build();
    }

    /**
     * Set the name of the gRPC {@link io.grpc.Channel} bean.
     *
     * @param channelName the name of the gRPC {@link io.grpc.Channel} bean
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Set the name of the {@link com.tangosol.io.Serializer}.
     *
     * @param serializer the name of the {@link com.tangosol.io.Serializer}
     */
    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    /**
     * Set whether distributed tracing should be enabled.
     *
     * @param enabled {@code true} to enable distributed tracing
     */
    public void setTracing(boolean enabled) {
        this.tracingEnabled = enabled;
    }

    /**
     * Set the priority for this configuration.
     * <p>{@link com.tangosol.net.Session Sessions} are started lowest priority first
     * and closed in reverse order.</p>
     *
     * @param priority the priority for this configuration
     * @see com.tangosol.net.SessionConfiguration#getPriority()
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
