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
package io.micronaut.coherence.discovery;

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false, propertySources = {"classpath:bootstrap.yaml"}, environments = "backend")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoherenceConfigurationClientTest {

    @Inject
    ApplicationContext context;

    @Inject
    @Name("config")
    Session session;

    @Inject
    CoherenceConfigurationClient client;

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

        NamedMap<String, Object> myAppMap = session.getMap("hello-world");
        myAppMap.put("hello", "hello-world hello");
        myAppMap.put("foo", "hello-world foo");

        NamedMap<String, Object> myAppTestMap = session.getMap("hello-world-test");
        myAppTestMap.put("hello", "hello-world test hello");

        NamedMap<String, Object> myAppBackendMap = session.getMap("hello-world-backend");
        myAppBackendMap.put("hello", "hello-world backend hello");
        myAppBackendMap.put("hello/bar", "hello-world backend hello/bar");

        NamedMap<String, Object> otherAppBackendMap = session.getMap("other-app");
        otherAppBackendMap.put("hello", "other-app hello");

        Publisher<PropertySource> propertySourcePublisher = client.getPropertySources(context.getEnvironment());
        Iterable<PropertySource> propsIt = Flux.from(propertySourcePublisher).toIterable();

        Map<String, PropertySource> propertySources = StreamSupport.stream(propsIt.spliterator(), false)
                .collect(Collectors.toMap(PropertySource::getName, Function.identity()));

        assertThat(propertySources.keySet().toArray(),
                arrayContainingInAnyOrder(
                        "application",
                        "application-test",
                        "application-backend",
                        "hello-world",
                        "hello-world-test",
                        "hello-world-backend"));

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

        PropertySource myAppPs = propertySources.get("hello-world");
        assertEquals(-98, myAppPs.getOrder());
        props = StreamSupport.stream(myAppPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "hello-world hello");
        expected.put("foo", "hello-world foo");
        assertEquals(expected, props);

        PropertySource myAppTestPs = propertySources.get("hello-world-test");
        assertEquals(-46, myAppTestPs.getOrder());
        props = StreamSupport.stream(myAppTestPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppTestPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "hello-world test hello");
        assertEquals(expected, props);

        PropertySource myAppBackendPs = propertySources.get("hello-world-backend");
        assertEquals(-44, myAppBackendPs.getOrder());
        props = StreamSupport.stream(myAppBackendPs.spliterator(), false)
                .collect(Collectors.toMap(Function.identity(), key -> (String) myAppBackendPs.get(key)));
        expected = new HashMap<>();
        expected.put("hello", "hello-world backend hello");
        expected.put("hello/bar", "hello-world backend hello/bar");
        assertEquals(expected, props);
    }
}
