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

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.oracle.coherence.common.util.Options;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.net.options.WithConfiguration;
import com.tangosol.net.options.WithName;
import com.tangosol.net.options.WithScopeName;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false, propertySources = "classpath:session-test.yaml")
class SessionConfigurationBeanTest {

    @Inject
    ApplicationContext ctx;

    @Test
    void shouldConfigureSessions() {
        Map<String, SessionConfiguration> beans = ctx.getBeansOfType(SessionConfiguration.class)
                .stream()
                .collect(Collectors.toMap(SessionConfiguration::getName, cfg -> cfg));

        assertThat(beans.size(), is(3));

        SessionConfiguration beanFoo = beans.get("foo");
        assertThat(beanFoo, is(notNullValue()));
        assertThat(beanFoo.getScopeName(), is("Foo"));
        Options<Session.Option> optionsFoo = Options.from(Session.Option.class, beanFoo.getOptions());
        assertThat(optionsFoo.get(WithName.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithName.class).getName(), is("foo"));
        assertThat(optionsFoo.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithScopeName.class).getScopeName(), is("Foo"));
        assertThat(optionsFoo.get(WithConfiguration.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithConfiguration.class).getLocation(), is("foo-config.xml"));

        SessionConfiguration beanBar = beans.get("bar");
        assertThat(beanBar, is(notNullValue()));
        assertThat(beanBar.getScopeName(), is("bar"));
        Options<Session.Option> optionsBar = Options.from(Session.Option.class, beanBar.getOptions());
        assertThat(optionsBar.get(WithName.class), is(notNullValue()));
        assertThat(optionsBar.get(WithName.class).getName(), is("bar"));
        assertThat(optionsBar.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsBar.get(WithScopeName.class).getScopeName(), is("bar"));
        assertThat(optionsBar.get(WithConfiguration.class, null), is(nullValue()));

        SessionConfiguration beanDefault = beans.get(Coherence.DEFAULT_NAME);
        assertThat(beanDefault, is(notNullValue()));
        assertThat(beanDefault.getScopeName(), is(Coherence.DEFAULT_SCOPE));
        Options<Session.Option> optionsDefault = Options.from(Session.Option.class, beanDefault.getOptions());
        assertThat(optionsDefault.get(WithName.class), is(notNullValue()));
        assertThat(optionsDefault.get(WithName.class).getName(), is(Coherence.DEFAULT_NAME));
        assertThat(optionsDefault.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsDefault.get(WithScopeName.class).getScopeName(), is(Coherence.DEFAULT_SCOPE));
        assertThat(optionsDefault.get(WithConfiguration.class), is(notNullValue()));
        assertThat(optionsDefault.get(WithConfiguration.class).getLocation(), is("coherence-config.xml"));
    }
}
