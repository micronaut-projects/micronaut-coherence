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

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import io.micronaut.coherence.annotation.Name;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import io.reactivex.Flowable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;

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
    @Name("config")
    Session session;

    @Test
    public void shouldProvidePropertySources() {
        NamedMap<String, Object> applicationMap = session.getMap("application");
        applicationMap.put("hello", "app hello");
        applicationMap.put("foo", "app foo");
        applicationMap.put("my-app", "app my-app");
        applicationMap.put("test", "app test");
        applicationMap.put("backend", "app backend");

        NamedMap<String, Object> applicationFooMap = session.getMap("application-foo");
        applicationFooMap.put("bar", "app foo bar");

        NamedMap<String, Object> applicationTestMap = session.getMap("application-test");
        applicationTestMap.put("foo", "app test foo");

        NamedMap<String, Object> applicationBackendMap = session.getMap("application-backend");
        applicationBackendMap.put("foo", "app backend foo");
        applicationBackendMap.put("bar", "app backend bar");

        NamedMap<String, Object> myAppMap = session.getMap("my-app");
        myAppMap.put("hello", "my-app hello");
        myAppMap.put("foo", "my-app foo");

        NamedMap<String, Object> myAppTestMap = session.getMap("my-app-test");
        myAppTestMap.put("hello", "my-app test hello");

        NamedMap<String, Object> myAppBackendMap = session.getMap("my-app-backend");
        myAppBackendMap.put("hello", "my-app backend hello");
        myAppBackendMap.put("hello/bar", "my-app backend hello/bar");

        NamedMap<String, Object> otherAppBackendMap = session.getMap("other-app");
        otherAppBackendMap.put("hello", "other-app hello");

        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setName("my-app");
        CoherenceClientConfiguration clientConfig = new CoherenceClientConfiguration();
        CoherenceConfigurationClient client = new CoherenceConfigurationClient(applicationConfiguration, clientConfig) {
            @Override
            protected Session buildSession(CoherenceClientConfiguration coherenceClientConfiguration) {
                Session session = mock(Session.class);
                when(session.<String, Object>getMap("application"))
                        .thenReturn(applicationMap);
                when(session.<String, Object>getMap("application-foo"))
                        .thenReturn(applicationFooMap);
                when(session.<String, Object>getMap("application-test"))
                        .thenReturn(applicationTestMap);
                when(session.<String, Object>getMap("application-backend"))
                        .thenReturn(applicationBackendMap);
                when(session.<String, Object>getMap("my-app"))
                        .thenReturn(myAppMap);
                when(session.<String, Object>getMap("my-app-test"))
                        .thenReturn(myAppTestMap);
                when(session.<String, Object>getMap("my-app-backend"))
                        .thenReturn(myAppBackendMap);
                when(session.<String, Object>getMap("other-app"))
                        .thenReturn(otherAppBackendMap);
                return session;
            }
        };

        Publisher<PropertySource> propertySourcePublisher = client.getPropertySources(context.getEnvironment());
        Iterable<PropertySource> propsIt = Flowable.fromPublisher(propertySourcePublisher)
                .blockingIterable();

        Map<String, PropertySource> propertySources = StreamSupport.stream(propsIt.spliterator(), false)
                .collect(Collectors.toMap(PropertySource::getName, Function.identity()));

        assertThat(propertySources.keySet().toArray(),
                arrayContainingInAnyOrder(
                        "application",
                        "application-test",
                        "application-backend",
                        "my-app",
                        "my-app-test",
                        "my-app-backend"));

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

        PropertySource appTestPs = propertySources.get("application-test");
        assertEquals(-47, appTestPs.getOrder());
        props = StreamSupport.stream(appTestPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) appTestPs.get(key)));
        expected = new HashMap<>();
        expected.put("foo", "app test foo");
        assertEquals(expected, props);

        PropertySource appBackendPs = propertySources.get("application-backend");
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
        expected.put("foo", "my-app foo");
        assertEquals(expected, props);

        PropertySource myAppTestPs = propertySources.get("my-app-test");
        assertEquals(-46, myAppTestPs.getOrder());
        props = StreamSupport.stream(myAppTestPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppTestPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "my-app test hello");
        assertEquals(expected, props);

        PropertySource myAppBackendPs = propertySources.get("my-app-backend");
        assertEquals(-44, myAppBackendPs.getOrder());
        props = StreamSupport.stream(myAppBackendPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppBackendPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "my-app backend hello");
        expected.put("hello/bar", "my-app backend hello/bar");
        assertEquals(expected, props);
    }

    @Test
    public void shouldCreateDefaultChannel() {
        ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
        CoherenceClientConfiguration coherenceClientConfiguration = new CoherenceClientConfiguration();
        coherenceClientConfiguration.setEnableTls(true);
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
