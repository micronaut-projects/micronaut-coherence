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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.AlwaysFilter;
import com.oracle.coherence.inject.FilterBinding;
import com.oracle.coherence.inject.FilterFactory;
import com.oracle.coherence.inject.WhereFilter;

import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false)
class FilterFactoriesTest {

    @Inject
    @AlwaysFilter
    Filter<?> filterOne;

    @Inject
    @WhereFilter("foo=1")
    Filter<?> filterTwo;

    @Inject
    @CustomFilter("testing")
    Filter<?> filterThree;

    @FilterBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomFilter {
        String value();
    }

    @Test
    void shouldInjectAlwaysFilter() {
        assertThat(filterOne, is(instanceOf(com.tangosol.util.filter.AlwaysFilter.class)));
    }

    @Test
    void shouldInjectWhereFilter() {
        Filter<?> expected = QueryHelper.createFilter("foo=1");
        assertThat(filterTwo, is(expected));
    }

    @Test
    void shouldInjectCustomFilter() {
        assertThat(filterThree, is(instanceOf(FilterStub.class)));
        assertThat(((FilterStub<?>) filterThree).getValue(), is("testing"));
    }


    @Singleton
    @CustomFilter("")
    public static class CustomFactory<T> implements FilterFactory<CustomFilter, T> {
        @Override
        public Filter<T> create(CustomFilter annotation) {
            return new FilterStub<>(annotation.value());
        }
    }

    static class FilterStub<T>
            implements Filter<T> {

        private final String value;

        FilterStub(String sValue) {
            value = sValue;
        }

        @Override
        public boolean evaluate(T o) {
            return true;
        }

        String getValue() {
            return value;
        }
    }
}
