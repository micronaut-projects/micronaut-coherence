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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.coherence.annotation.AlwaysFilter;
import io.micronaut.coherence.annotation.ChainedExtractor;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.annotation.PropertyExtractor;
import io.micronaut.coherence.annotation.SessionName;
import io.micronaut.coherence.annotation.View;
import io.micronaut.coherence.annotation.WhereFilter;

import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.Filters;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;

import data.Person;
import data.PhoneNumber;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml", environments = "NamedCacheFactoriesViewTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("rawtypes")
class NamedCacheFactoriesViewTest {

    @Inject
    ApplicationContext ctx;

    @Test
    void shouldInjectContinuousQueryCacheUsingFieldName() {
        ContinuousQueryCacheFieldsBean bean = ctx.getBean(ContinuousQueryCacheFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getNumbers().getCache().getCacheName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedNamedCache() {
        ContinuousQueryCacheFieldsBean bean = ctx.getBean(ContinuousQueryCacheFieldsBean.class);
        assertThat(bean.getNamedCache(), is(notNullValue()));
        assertThat(bean.getNamedCache(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getNamedCache().getCache().getCacheName(), is("numbers"));
    }

    @Test
    void shouldInjectContinuousQueryCacheWithGenerics() {
        ContinuousQueryCacheFieldsBean bean = ctx.getBean(ContinuousQueryCacheFieldsBean.class);
        assertThat(bean.getGenericCache(), is(notNullValue()));
        assertThat(bean.getGenericCache(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getGenericCache().getCache().getCacheName(), is("numbers"));
    }

    @Test
    void shouldInjectContinuousQueryCacheWithGenericKeys() {
        ContinuousQueryCacheFieldsBean bean = ctx.getBean(ContinuousQueryCacheFieldsBean.class);
        assertThat(bean.getGenericKeys(), is(notNullValue()));
        assertThat(bean.getGenericKeys(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getGenericKeys().getCache().getCacheName(), is("genericKeys"));
    }

    @Test
    void shouldInjectContinuousQueryCacheWithGenericValues() {
        ContinuousQueryCacheFieldsBean bean = ctx.getBean(ContinuousQueryCacheFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getGenericValues().getCache().getCacheName(), is("genericValues"));
    }

    @Test
    void shouldInjectCachesFromDifferentSessions() {
        DifferentSessionsBean bean = ctx.getBean(DifferentSessionsBean.class);

        assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
        assertThat(bean.getDefaultCcfNumbers(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getDefaultCcfNumbers().getCache().getCacheName(), is("numbers"));

        assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
        assertThat(bean.getSpecificCcfNumbers(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getSpecificCcfNumbers().getCache().getCacheName(), is("numbers"));

        assertThat(bean.getDefaultCcfNumbers().getCache().getCacheService(),
                   is(not(bean.getSpecificCcfNumbers().getCache().getCacheService())));
    }

    @Test
    void testCtorInjection() {
        CtorBean bean = ctx.getBean(CtorBean.class);

        assertThat(bean.getNumbers(), notNullValue());
        assertThat(bean.getNumbers(), is(instanceOf(ContinuousQueryCache.class)));
        assertThat(bean.getNumbers().getCache().getCacheName(), is("numbers"));
    }

    @Test
    void shouldInjectSuperTypeContinuousQueryCache() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ContinuousQueryCache cache = bean.getContinuousQueryCache();
        assertThat(cache, is(notNullValue()));
        assertThat(cache, is(sameInstance(bean.getContinuousQueryCache())));
    }

    @Test
    void shouldInjectSuperTypeNamedCache() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        NamedCache cache = bean.getNamedCache();
        assertThat(cache, is(notNullValue()));
    }

    @Test
    void shouldInjectSuperTypeInvocableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        InvocableMap map = bean.getInvocableMap();
        assertThat(map, is(notNullValue()));
    }

    @Test
    void shouldInjectSuperTypeObservableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ObservableMap map = bean.getObservableMap();
        assertThat(map, is(notNullValue()));
    }

    @Test
    void shouldInjectSuperTypeConcurrentMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ConcurrentMap map = bean.getConcurrentMap();
        assertThat(map, is(notNullValue()));
    }

    @Test
    void shouldInjectSuperTypeQueryMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        QueryMap map = bean.getQueryMap();
        assertThat(map, is(notNullValue()));
    }

    @Test
    void shouldInjectSuperTypeCacheMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        CacheMap map = bean.getCacheMap();
        assertThat(map, is(notNullValue()));
    }

    @Test
    void shouldInjectContinuousQueryCacheWithFilters() {
        ContinuousQueryCacheWithFiltersBean withFilters = ctx.getBean(ContinuousQueryCacheWithFiltersBean.class);
        NamedCache<String, Person> cache = withFilters.getCache();
        ContinuousQueryCache<String, Person, Person> always = withFilters.getAlways();
        ContinuousQueryCache<String, Person, Person> foo = withFilters.getFoo();

        // populate the underlying cache
        populate(cache);
        assertThat(always.size(), is(cache.size()));

        Set<Map.Entry<String, Person>> entries = cache.entrySet(Filters.equal("lastName", "foo"));
        assertThat(foo.size(), is(entries.size()));
        for (Map.Entry<String, Person> entry : entries) {
            MatcherAssert.assertThat(foo.get(entry.getKey()), CoreMatchers.is(entry.getValue()));
        }
    }

    @Test
    void shouldInjectContinuousQueryCacheWithTransformer() {
        WithTransformersBean bean = ctx.getBean(WithTransformersBean.class);
        NamedCache<String, Person> cache = bean.getNamedCache();
        ContinuousQueryCache<String, Person, String> names = bean.getNames();

        // populate the underlying cache
        populate(cache);

        assertThat(names.size(), is(cache.size()));
        for (Map.Entry<String, Person> entry : cache.entrySet()) {
            MatcherAssert.assertThat(names.get(entry.getKey()), CoreMatchers.is(entry.getValue().getFirstName()));
        }
    }

    @Test
    void shouldInjectContinuousQueryCacheWithTransformerAndFilter() {
        WithTransformersBean bean = ctx.getBean(WithTransformersBean.class);
        NamedCache<String, Person> cache = bean.getNamedCache();
        ContinuousQueryCache<String, Person, String> filtered = bean.getFilteredNames();

        // populate the underlying cache
        populate(cache);

        Set<Map.Entry<String, Person>> entries = cache.entrySet(Filters.equal("lastName", "foo"));
        assertThat(filtered.size(), is(entries.size()));
        for (Map.Entry<String, Person> entry : entries) {
            MatcherAssert
                    .assertThat(filtered.get(entry.getKey()), CoreMatchers.is(entry.getValue().getPhoneNumber().getNumber()));
        }
    }

    @Test
    void shouldInjectContinuousQueryCacheWithKeysOnly() {
        WithTransformersBean bean = ctx.getBean(WithTransformersBean.class);
        NamedCache<String, Person> cache = bean.getNamedCache();
        ContinuousQueryCache<String, Person, String> keysOnly = bean.getKeysOnly();

        // populate the underlying cache
        populate(cache);

        assertThat(keysOnly.size(), is(cache.size()));
        assertThat(keysOnly.isCacheValues(), is(false));
    }

    private void populate(NamedCache<String, Person> cache) {
        for (int i = 0; i < 100; i++) {
            String lastName = (i % 2 == 0) ? "foo" : "bar";
            Person bean = new Person(String.valueOf(i),
                                     lastName,
                                     LocalDate.now(),
                                     new PhoneNumber(44, "12345" + i));

            cache.put(lastName + "-" + i, bean);
        }
    }

    // ----- test beans -----------------------------------------------------

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class ContinuousQueryCacheFieldsBean {
        @Inject
        private ContinuousQueryCache numbers;

        @Inject
        @Name("numbers")
        @View
        private ContinuousQueryCache namedCache;

        @Inject
        @Name("numbers")
        @View
        private ContinuousQueryCache<Integer, String, String> genericCache;

        @Inject
        @View
        private ContinuousQueryCache<List<String>, String, String> genericKeys;

        @Inject
        @View
        private ContinuousQueryCache<String, List<String>, String> genericValues;

        public ContinuousQueryCache getNumbers() {
            return numbers;
        }

        public ContinuousQueryCache getNamedCache() {
            return namedCache;
        }

        public ContinuousQueryCache<Integer, String, String> getGenericCache() {
            return genericCache;
        }

        public ContinuousQueryCache<List<String>, String, String> getGenericKeys() {
            return genericKeys;
        }

        public ContinuousQueryCache<String, List<String>, String> getGenericValues() {
            return genericValues;
        }
    }

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class ContinuousQueryCacheWithFiltersBean {
        @Inject
        private NamedCache<String, Person> beans;

        @Inject
        @AlwaysFilter
        @Name("beans")
        @View
        private ContinuousQueryCache<String, Person, Person> always;

        @Inject
        @WhereFilter("lastName = 'foo'")
        @Name("beans")
        @View
        private ContinuousQueryCache<String, Person, Person> foo;

        public NamedCache<String, Person> getCache() {
            return beans;
        }

        public ContinuousQueryCache<String, Person, Person> getAlways() {
            return always;
        }

        public ContinuousQueryCache<String, Person, Person> getFoo() {
            return foo;
        }
    }

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class DifferentSessionsBean {
        @Inject
        @Name("numbers")
        @View
        private ContinuousQueryCache defaultCcfNumbers;

        @Inject
        @Name("numbers")
        @View
        @SessionName("test")
        private ContinuousQueryCache specificCcfNumbers;

        public ContinuousQueryCache getDefaultCcfNumbers() {
            return defaultCcfNumbers;
        }

        public ContinuousQueryCache getSpecificCcfNumbers() {
            return specificCcfNumbers;
        }
    }

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class CtorBean {
        private final NamedCache<Integer, String> view;

        private final ContinuousQueryCache<Integer, String, String> numbers;

        @Inject
        CtorBean(@Name("numbers") @View NamedCache<Integer, String> view,
                 @Name("numbers") ContinuousQueryCache<Integer, String, String> numbers) {
            this.view = view;
            this.numbers = numbers;
        }

        NamedCache<Integer, String> getView() {
            return view;
        }

        ContinuousQueryCache<Integer, String, String> getNumbers() {
            return numbers;
        }
    }

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class SuperTypesBean {
        @Inject
        @Name("numbers")
        @View
        private ContinuousQueryCache<Integer, String, String> cqc;

        @Inject
        @Name("numbers")
        @View
        private NamedCache<Integer, String> namedCache;

        @Inject
        @Name("numbers")
        @View
        private InvocableMap<Integer, String> invocableMap;

        @Inject
        @Name("numbers")
        @View
        private ObservableMap<Integer, String> observableMap;

        @Inject
        @Name("numbers")
        @View
        private ConcurrentMap<Integer, String> concurrentMap;

        @Inject
        @Name("numbers")
        @View
        private QueryMap<Integer, String> queryMap;

        @Inject
        @Name("numbers")
        @View
        private CacheMap<Integer, String> cacheMap;

        ContinuousQueryCache<Integer, String, String> getContinuousQueryCache() {
            return cqc;
        }

        NamedCache<Integer, String> getNamedCache() {
            return namedCache;
        }

        InvocableMap<Integer, String> getInvocableMap() {
            return invocableMap;
        }

        ObservableMap<Integer, String> getObservableMap() {
            return observableMap;
        }

        ConcurrentMap<Integer, String> getConcurrentMap() {
            return concurrentMap;
        }

        QueryMap<Integer, String> getQueryMap() {
            return queryMap;
        }

        CacheMap<Integer, String> getCacheMap() {
            return cacheMap;
        }
    }

    @Singleton
    @Requires(env = "NamedCacheFactoriesViewTest")
    static class WithTransformersBean {
        @Inject
        @Name("people")
        private NamedCache<String, Person> namedCache;

        @Inject
        @Name("people")
        @View(cacheValues = false)
        private ContinuousQueryCache<String, Person, String> keysOnly;

        @Inject
        @Name("people")
        @View
        @PropertyExtractor("firstName")
        private ContinuousQueryCache<String, Person, String> names;

        @Inject
        @Name("people")
        @View
        @ChainedExtractor({"phoneNumber", "number"})
        @WhereFilter("lastName = 'foo'")
        private ContinuousQueryCache<String, Person, String> filteredNames;

        NamedCache<String, Person> getNamedCache() {
            return namedCache;
        }

        ContinuousQueryCache<String, Person, String> getNames() {
            return names;
        }

        ContinuousQueryCache<String, Person, String> getFilteredNames() {
            return filteredNames;
        }

        ContinuousQueryCache<String, Person, String> getKeysOnly() {
            return keysOnly;
        }
    }
}
