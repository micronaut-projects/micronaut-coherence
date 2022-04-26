/*
 * Copyright 2017-2022 original authors
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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.oracle.coherence.client.GrpcSessionConfiguration;
import com.tangosol.io.Serializer;
import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;

import io.grpc.Channel;
import io.micronaut.coherence.SessionConfigurationProvider;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:session-test.yaml")
class GrpcSessionConfigurationBeanTest {

    @Inject
    ApplicationContext ctx;

    @Test
    void shouldConfigureSessions() {
        Map<String, SessionConfiguration> beans = ctx.getBeansOfType(GrpcSessionConfigurationBean.class)
                .stream()
                .map(SessionConfigurationProvider::getConfiguration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(SessionConfiguration::getName, cfg -> cfg));

        Optional<Channel> optional = ctx.findBean(Channel.class, Qualifiers.byName("default"));
        assertThat(optional.isPresent(), is(true));

        assertThat(beans.size(), is(2));

        GrpcSessionConfiguration beanBar = (GrpcSessionConfiguration) beans.get("bar");
        assertThat(beanBar, is(notNullValue()));
        assertThat(beanBar.getName(), is("bar"));
        assertThat(beanBar.getScopeName(), is(Coherence.DEFAULT_SCOPE));
        assertThat(beanBar.getSerializer().orElseThrow(AssertionFailedError::new), notNullValue());
        assertThat(beanBar.getFormat().orElseThrow(AssertionFailedError::new), is("java"));
        assertThat(beanBar.enableTracing(), is(true));

        GrpcSessionConfiguration beanBaz = (GrpcSessionConfiguration) beans.get("baz");
        assertThat(beanBaz, is(notNullValue()));
        assertThat(beanBaz.getName(), is("baz"));
        assertThat(beanBaz.getScopeName(), is("scoped"));
        assertThat(beanBaz.getSerializer().isPresent(), is(false));
        assertThat(beanBaz.getFormat().orElseThrow(AssertionFailedError::new), is("binary"));
        assertThat(beanBaz.enableTracing(), is(false));
    }
}
