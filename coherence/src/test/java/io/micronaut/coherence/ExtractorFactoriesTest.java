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


import com.oracle.coherence.inject.ChainedExtractor;
import com.oracle.coherence.inject.ExtractorBinding;
import com.oracle.coherence.inject.ExtractorFactory;
import com.oracle.coherence.inject.PofExtractor;
import com.oracle.coherence.inject.PropertyExtractor;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.cache.BackingMapBinaryEntry;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMapHelper;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import data.Person;
import data.PhoneNumber;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for filter producers and annotations.
 *
 * @author Vaso Putica 2020.11.26
 */
@SuppressWarnings("unchecked")
@MicronautTest
class ExtractorFactoriesTest {

    private ConfigurablePofContext pofContext = new ConfigurablePofContext("pof-config.xml");

    private Person person;

    private PhoneNumber phoneNumber;

    private Binary binaryKey;

    private Binary binaryPerson;

    private Map.Entry<String, Person> entry;

    @BeforeEach
    void setup() {
        phoneNumber = new PhoneNumber(44, "04242424242");
        person = new Person("Arthur", "Dent",
                LocalDate.of(1978, 3, 8),
                phoneNumber);

        binaryKey = ExternalizableHelper.toBinary("AD", pofContext);
        binaryPerson = ExternalizableHelper.toBinary(person, pofContext);

        BackingMapContext ctx = mock(BackingMapContext.class);
        Map<ValueExtractor, MapIndex> index = new HashMap<>();

        when(ctx.getIndexMap()).thenReturn(index);

        entry = new BackingMapBinaryEntry(binaryKey, binaryPerson, binaryPerson, null) {
            @Override
            public Object getKey() {
                return "AD";
            }

            @Override
            public Object getValue() {
                return person;
            }

            @Override
            public BackingMapContext getBackingMapContext() {
                return ctx;
            }

            @Override
            public Serializer getSerializer() {
                return pofContext;
            }
        };
    }

    @Inject
    ApplicationContext context; //refers to the current application context within the scope of the test

    @Test
    void shouldInjectPropertyExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getFirstNameExtractor(), is(notNullValue()));

        String value = InvocableMapHelper.extractFromEntry(bean.getFirstNameExtractor(), entry);
        assertThat(value, is(person.getFirstName()));
    }

    @Test
    void shouldInjectMultiPropertyExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getMultiPropertyExtractor(), is(notNullValue()));

        List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiPropertyExtractor(), entry);
        assertThat(value, contains(person.getFirstName(), person.getLastName()));
    }

    @Test
    void shouldInjectChainedExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getChainedExtractor(), is(notNullValue()));

        String value = InvocableMapHelper.extractFromEntry(bean.getChainedExtractor(), entry);
        assertThat(value, is(person.getPhoneNumber().getNumber()));
    }

    @Test
    void shouldInjectMultiChainedExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getMultiChainedExtractor(), is(notNullValue()));

        List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiChainedExtractor(), entry);
        assertThat(value, contains(phoneNumber.getCountryCode(), phoneNumber.getNumber()));
    }

    @Test
    void shouldInjectCustomExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getCustomExtractor(), is(notNullValue()));

        String value = InvocableMapHelper.extractFromEntry(bean.getCustomExtractor(), entry);
        assertThat(value, is(person.getLastName()));
    }

    @Test
    void shouldInjectPofExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getPofExtractor(), is(notNullValue()));

        Integer value = InvocableMapHelper.extractFromEntry(bean.getPofExtractor(), entry);
        assertThat(value, is(person.getPhoneNumber().getCountryCode()));
    }

    @Test
    void shouldInjectMultiPofExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getMultiPofExtractor(), is(notNullValue()));

        List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiPofExtractor(), entry);
        assertThat(value, contains(phoneNumber.getCountryCode(), phoneNumber.getNumber()));
    }

    @Test
    void shouldInjectMultiExtractor() {
        ExtractorBean bean = context.getBean(ExtractorBean.class);
        assertThat(bean, is(notNullValue()));
        assertThat(bean.getMultiExtractor(), is(notNullValue()));

        List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiExtractor(), entry);
        assertThat(value, contains(person.getLastName(),
                person.getFirstName(),
                person.getPhoneNumber().getNumber()));
    }

    // ----- helper classes -------------------------------------------------

    @Inherited
    @ExtractorBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestExtractor {
    }

    @TestExtractor
    @Singleton
    public static class TestExtractorFactory
            implements ExtractorFactory<TestExtractor, Person, String> {
        @Override
        public ValueExtractor<Person, String> create(TestExtractor annotation) {
            return Person::getLastName;
        }
    }

    @Singleton
    private static class ExtractorBean {
        public ExtractorBean() {
        }

        @Inject
        @PropertyExtractor("firstName")
        private ValueExtractor<Person, String> firstNameExtractor;

        @Inject
        @ChainedExtractor({"phoneNumber", "number"})
        private ValueExtractor<Person, String> chainedExtractor;

        @Inject
        @TestExtractor
        private ValueExtractor<Person, String> customExtractor;

        @Inject
        @PofExtractor(index = {3, 0})
        private ValueExtractor<Person, Integer> pofExtractor;

        @Inject
        @TestExtractor
        @PropertyExtractor("firstName")
        @ChainedExtractor({"phoneNumber", "number"})
        private ValueExtractor<Person, List<?>> multiExtractor;

        @Inject
        @PropertyExtractor("firstName")
        @PropertyExtractor("lastName")
        private ValueExtractor<Person, List<?>> multiPropertyExtractor;

        @Inject
        @ChainedExtractor({"phoneNumber", "countryCode"})
        @ChainedExtractor({"phoneNumber", "number"})
        private ValueExtractor<Person, List<?>> multiChainedExtractor;

        @Inject
        @PofExtractor(index = {3, 0})
        @PofExtractor(index = {3, 1})
        private ValueExtractor<Person, List<?>> multiPofExtractor;

        public ValueExtractor<Person, String> getFirstNameExtractor() {
            return firstNameExtractor;
        }

        public ValueExtractor<Person, String> getChainedExtractor() {
            return chainedExtractor;
        }

        public ValueExtractor<Person, String> getCustomExtractor() {
            return customExtractor;
        }

        public ValueExtractor<Person, Integer> getPofExtractor() {
            return pofExtractor;
        }

        public ValueExtractor<Person, List<?>> getMultiExtractor() {
            return multiExtractor;
        }

        public ValueExtractor<Person, List<?>> getMultiPropertyExtractor() {
            return multiPropertyExtractor;
        }

        public ValueExtractor<Person, List<?>> getMultiChainedExtractor() {
            return multiChainedExtractor;
        }

        public ValueExtractor<Person, List<?>> getMultiPofExtractor() {
            return multiPofExtractor;
        }
    }
}
