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
package io.micronaut.coherence.discovery;


import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * The Coherence client configuration.
 */
@ConfigurationProperties(CoherenceClientConfiguration.PREFIX)
@BootstrapContextCompatible
public class CoherenceClientConfiguration {
    public static final String PREFIX = "coherence.client";

    private String host = "localhost";
    private int port = 1408;
    private boolean enableTls;

    /**
     * Returns host name of gRPC server.
     *
     * @return host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets host name of gRPC server.
     *
     * @param host host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets gRPC server port.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets gRPC server port.
     *
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns true if TLS is enabled.
     *
     * @return true if TLS is enabled
     */
    public boolean isEnableTls() {
        return enableTls;
    }

    /**
     * Enables TLS support.
     *
     * @param enableTls  {@code true} to enable TLS
     */
    public void setEnableTls(boolean enableTls) {
        this.enableTls = enableTls;
    }

    @Override
    public String toString() {
        return "CoherenceClientConfiguration{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", enableTls=" + enableTls +
                '}';
    }
}
