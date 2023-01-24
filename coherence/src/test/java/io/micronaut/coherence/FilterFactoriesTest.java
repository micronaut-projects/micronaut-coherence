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
package io.micronaut.coherence;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import io.micronaut.coherence.annotation.AlwaysFilter;
import io.micronaut.coherence.annotation.FilterBinding;
import io.micronaut.coherence.annotation.WhereFilter;

import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;
import com.tangosol.util.filter.AllFilter;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Inject
    @CustomFilterTwo("four")
    Filter<?> filterFour;

    @Inject
    @CustomFilterTwo("five.1")
    @CustomFilterTwo("five.2")
    Filter<?> filterFive;

    @FilterBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomFilter {
        String value();
    }

    @FilterBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomFilterTwoHolder {
        CustomFilterTwo[] value() default {};
    }

    @FilterBinding
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(CustomFilterTwoHolder.class)
    public @interface CustomFilterTwo {
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

    @Test
    void shouldInjectRepeatebleQualifiedFilter() {
        assertThat(filterFour, is(instanceOf(FilterStub.class)));
        assertThat(((FilterStub<?>) filterFour).getValue(), is("four"));
    }

    @Test
    void shouldInjectRepeatebleHolderQualifiedFilter() {
        assertThat(filterFive, is(instanceOf(AllFilter.class)));
        Filter<?>[] filters = ((AllFilter) filterFive).getFilters();
        assertThat(filters.length, is(2));
        assertThat(filters[0], is(instanceOf(FilterStub.class)));
        assertThat(((FilterStub<?>) filters[0]).getValue(), is("five.1"));
        assertThat(filters[1], is(instanceOf(FilterStub.class)));
        assertThat(((FilterStub<?>) filters[1]).getValue(), is("five.2"));
    }


    @Singleton
    @CustomFilter("")
    public static class CustomFactory<T> implements FilterFactory<CustomFilter, T> {
        @Override
        public Filter<T> create(CustomFilter annotation) {
            return new FilterStub<>(annotation.value());
        }
    }


    @Singleton
    @CustomFilterTwo("")
    public static class CustomFactoryTwo<T> implements FilterFactory<CustomFilterTwo, T> {
        @Override
        public Filter<T> create(CustomFilterTwo annotation) {
            return new FilterStub<>(annotation.value());
        }
    }

    @Singleton
    @CustomFilterTwoHolder
    public static class CustomFactoryTwoX<T> implements FilterFactory<CustomFilterTwoHolder, T> {
        @SuppressWarnings("unchecked")
        @Override
        public Filter<T> create(CustomFilterTwoHolder holder) {
            Filter<T>[] filters = Arrays.stream(holder.value())
                    .map(ann -> new FilterStub<>(ann.value()))
                    .toArray(FilterStub[]::new);

            return filters.length == 1 ? filters[0] : new AllFilter(filters);
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
