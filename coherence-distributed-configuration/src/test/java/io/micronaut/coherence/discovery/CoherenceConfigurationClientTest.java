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

import com.oracle.coherence.client.GrpcSessionConfiguration;

import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import io.reactivex.Flowable;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest(startApplication = false, environments = "backend", propertySources = "classpath:sessions.yaml")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CoherenceConfigurationClientTest {

    @Inject
    ApplicationContext context;

    @Inject
    Session session;

    @Test
    public void shouldProvidePropertySources() {
        NamedCache<String, Object> cache = session.getCache("config-cache");

        cache.put("hello", "error");

        cache.put("application/hello", "app hello");
        cache.put("application/foo", "app foo");
        cache.put("application/", "invalid");
        cache.put("application/my-app", "app my-app");
        cache.put("application/test", "app test");
        cache.put("application/backend", "app backend");

        cache.put("application/foo/bar", "app foo bar");
        cache.put("application/test/foo", "app test foo");
        cache.put("application/backend/foo", "app backend foo");
        cache.put("application/backend/bar", "app backend bar");

        cache.put("my-app/", "my-app invalid");
        cache.put("my-app/hello", "my-app hello");
        cache.put("my-app/hello/foo", "my-app hello bar");
        cache.put("my-app/test", "my-app test");
        cache.put("my-app/test/", "my-app test invalid");
        cache.put("my-app/test/hello", "my-app test hello");
        cache.put("my-app/backend/hello", "my-app backend hello");
        cache.put("my-app/backend/hello/nope", "my-app backend hello nope invalid");

        cache.put("other-app/hello", "other-app hello invalid");

        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setName("my-app");
        CoherenceClientConfiguration clientConfig = new CoherenceClientConfiguration();
        CoherenceConfigurationClient client = new CoherenceConfigurationClient(applicationConfiguration, clientConfig) {
            @Override
            protected NamedCache<String, Object> getCache(String cacheName, CoherenceClientConfiguration coherenceClientConfiguration) {
                return cache;
            }
        };

        Publisher<PropertySource> propertySourcePublisher = client.getPropertySources(context.getEnvironment());
        Iterable<PropertySource> propsIt = Flowable.fromPublisher(propertySourcePublisher)
                .blockingIterable();

        Map<String, PropertySource> propertySources = StreamSupport.stream(propsIt.spliterator(), false)
                .collect(Collectors.toMap(PropertySource::getName, Function.identity()));

        assertThat(propertySources.keySet().toArray(),
                arrayContainingInAnyOrder("application",
                        "application/test",
                        "application/backend",
                        "my-app",
                        "my-app/test",
                        "my-app/backend"));

        PropertySource appPs = propertySources.get("application");
        assertEquals(-99, appPs.getOrder());
        Map<String, String> props = StreamSupport.stream(appPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) appPs.get(key)));
        Map<String, String> expected = new HashMap<>();
        expected.put("hello", "app hello");
        expected.put("foo", "app foo");
        expected.put("my-app", "app my-app");
        expected.put("test", "app test");
        expected.put("backend", "app backend");
        assertEquals(expected, props);

        PropertySource appTestPs = propertySources.get("application/test");
        assertEquals(-47, appTestPs.getOrder());
        props = StreamSupport.stream(appTestPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) appTestPs.get(key)));
        expected = new HashMap<>();
        expected.put("foo", "app test foo");
        assertEquals(expected, props);

        PropertySource appBackendPs = propertySources.get("application/backend");
        assertEquals(-45, appBackendPs.getOrder());
        props = StreamSupport.stream(appBackendPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) appBackendPs.get(key)));
        expected = new HashMap<>();
        expected.put("foo", "app backend foo");
        expected.put("bar", "app backend bar");
        assertEquals(expected, props);

        PropertySource myAppPs = propertySources.get("my-app");
        assertEquals(-98, myAppPs.getOrder());
        props = StreamSupport.stream(myAppPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "my-app hello");
        expected.put("test", "my-app test");
        assertEquals(expected, props);

        PropertySource myAppTestPs = propertySources.get("my-app/test");
        assertEquals(-46, myAppTestPs.getOrder());
        props = StreamSupport.stream(myAppTestPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppTestPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "my-app test hello");
        assertEquals(expected, props);

        PropertySource myAppBackendPs = propertySources.get("my-app/backend");
        assertEquals(-44, myAppBackendPs.getOrder());
        props = StreamSupport.stream(myAppBackendPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppBackendPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "my-app backend hello");
        assertEquals(expected, props);
    }

    @Test
    public void shouldCreateDefaultChannel() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        CoherenceClientConfiguration coherenceClientConfiguration = new CoherenceClientConfiguration();
        CoherenceConfigurationClient client = new CoherenceConfigurationClient(applicationConfiguration, coherenceClientConfiguration);

        ManagedChannelBuilder channelBuilder = mock(ManagedChannelBuilder.class);

        try (MockedStatic<ManagedChannelBuilder> builderStaticMock = Mockito.mockStatic(ManagedChannelBuilder.class)) {
            builderStaticMock.when(() -> ManagedChannelBuilder.forAddress(eq("localhost"), eq(1408)))
                    .thenReturn(channelBuilder);
            client.buildChannel(coherenceClientConfiguration);
            verify(channelBuilder, times(0)).usePlaintext();
            verify(channelBuilder).build();
        }
    }

    @Test
    public void shouldCreateConfiguredChannel() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        CoherenceClientConfiguration coherenceClientConfiguration = new CoherenceClientConfiguration();
        coherenceClientConfiguration.setHost("example.com");
        coherenceClientConfiguration.setPort(9999);
        coherenceClientConfiguration.setEnableTls(false);
        CoherenceConfigurationClient client = new CoherenceConfigurationClient(applicationConfiguration, coherenceClientConfiguration);

        ManagedChannelBuilder channelBuilder = mock(ManagedChannelBuilder.class);

        try (MockedStatic<ManagedChannelBuilder> builderStaticMock = Mockito.mockStatic(ManagedChannelBuilder.class)) {
            builderStaticMock.when(() -> ManagedChannelBuilder.forAddress(eq("example.com"), eq(9999)))
                    .thenReturn(channelBuilder);
            client.buildChannel(coherenceClientConfiguration);
            verify(channelBuilder).usePlaintext();
            verify(channelBuilder).build();
        }
    }

    @Test
    public void shouldCreateSession() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        CoherenceClientConfiguration coherenceClientConfiguration = new CoherenceClientConfiguration();
        CoherenceConfigurationClient client = new CoherenceConfigurationClient(applicationConfiguration, coherenceClientConfiguration);

        GrpcSessionConfiguration.Builder builder = mock(GrpcSessionConfiguration.Builder.class);
        GrpcSessionConfiguration grpcSessionConfiguration = mock(GrpcSessionConfiguration.class);
        when(builder.build()).thenReturn(grpcSessionConfiguration);

        Session session = mock(Session.class);

        try (MockedStatic<GrpcSessionConfiguration> grpcSessionConfigStaticMock = Mockito.mockStatic(GrpcSessionConfiguration.class);
             MockedStatic<Session> sessionStaticMock = Mockito.mockStatic(Session.class)) {

            grpcSessionConfigStaticMock.when(() -> GrpcSessionConfiguration.builder(any(Channel.class)))
                    .thenReturn(builder);

            sessionStaticMock.when(() -> Session.create(eq(grpcSessionConfiguration)))
                    .thenReturn(Optional.of(session));

            Session s = client.buildSession(coherenceClientConfiguration);
            assertEquals(session, s);

            verify(builder).build();
            sessionStaticMock.verify(() -> Session.create(grpcSessionConfiguration), times(1));
        }
    }
}
