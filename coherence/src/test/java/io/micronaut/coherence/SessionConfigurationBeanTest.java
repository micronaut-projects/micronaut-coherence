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
import javax.inject.Singleton;

import com.oracle.coherence.common.util.Options;

import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.net.options.WithConfiguration;
import com.tangosol.net.options.WithName;
import com.tangosol.net.options.WithScopeName;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false, propertySources = "classpath:session-test.yaml", environments = "SessionConfigurationBeanTest")
class SessionConfigurationBeanTest {

    @Inject
    ApplicationContext ctx;

    @Test
    void shouldConfigureSessions() {
        SessionConfiguration.Provider[] providers = ctx.getBeansOfType(SessionConfiguration.Provider.class)
                .toArray(new SessionConfiguration.Provider[0]);

        Map<String, SessionConfiguration> beans = CoherenceFactory.collectConfigurations(new SessionConfiguration[0], providers)
                .stream()
                .collect(Collectors.toMap(SessionConfiguration::getName, cfg -> cfg));

        assertThat(beans.size(), is(4));

        SessionConfiguration beanOne = beans.get("session-one");
        assertThat(beanOne, is(notNullValue()));
        assertThat(beanOne.getScopeName(), is("Foo"));
        Options<Session.Option> optionsFoo = Options.from(Session.Option.class, beanOne.getOptions());
        assertThat(optionsFoo.get(WithName.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithName.class).getName(), is("session-one"));
        assertThat(optionsFoo.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithScopeName.class).getScopeName(), is("Foo"));
        assertThat(optionsFoo.get(WithConfiguration.class), is(notNullValue()));
        assertThat(optionsFoo.get(WithConfiguration.class).getLocation(), is("foo-config.xml"));

        SessionConfiguration beanTwo = beans.get("SessionTwo");
        assertThat(beanTwo, is(notNullValue()));
        assertThat(beanTwo.getScopeName(), is("SessionTwo"));
        Options<Session.Option> optionsTwo = Options.from(Session.Option.class, beanTwo.getOptions());
        assertThat(optionsTwo.get(WithName.class), is(notNullValue()));
        assertThat(optionsTwo.get(WithName.class).getName(), is("SessionTwo"));
        assertThat(optionsTwo.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsTwo.get(WithScopeName.class).getScopeName(), is("SessionTwo"));
        assertThat(optionsTwo.get(WithConfiguration.class, null), is(nullValue()));

        SessionConfiguration beanThree = beans.get("session-three");
        assertThat(beanThree, is(notNullValue()));
        assertThat(beanThree.getScopeName(), is("session-three"));
        Options<Session.Option> optionsThree = Options.from(Session.Option.class, beanThree.getOptions());
        assertThat(optionsThree.get(WithName.class), is(notNullValue()));
        assertThat(optionsThree.get(WithName.class).getName(), is("session-three"));
        assertThat(optionsThree.get(WithScopeName.class), is(notNullValue()));
        assertThat(optionsThree.get(WithScopeName.class).getScopeName(), is("session-three"));
        assertThat(optionsThree.get(WithConfiguration.class), is(notNullValue()));
        assertThat(optionsThree.get(WithConfiguration.class).getLocation(), is("three.xml"));

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

    @Singleton
    @Requires(env = "SessionConfigurationBeanTest") // only enabled for this test
    @SessionConfigurationBean.Replaces
    static class Config implements SessionConfiguration.Provider {
        @Override
        public SessionConfiguration getConfiguration() {
            return SessionConfiguration.builder()
                    .named("session-three")
                    .withConfigUri("three.xml")
                    .build();
        }
    }
}
