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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;

import com.tangosol.net.AsyncNamedMap;
import com.tangosol.net.NamedMap;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml")
@SuppressWarnings("rawtypes")
class NamedCacheFactoriesNamedMapTest {

    @Inject 
    ApplicationContext ctx;

    @Test
    void shouldInjectAsyncNamedMapUsingFieldName() {
        AsyncNamedMapFieldsBean bean = ctx.getBean(AsyncNamedMapFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getNamedMap().getName(), is("numbers"));
    }

    @Test
    void shouldInjectAsyncNamedMapWithGenericKeys() {
        AsyncNamedMapFieldsBean bean = ctx.getBean(AsyncNamedMapFieldsBean.class);
        assertThat(bean.getGenericKeys(), is(notNullValue()));
        assertThat(bean.getGenericKeys().getNamedMap().getName(), is("genericKeys"));
    }

    @Test
    void shouldInjectAsyncNamedMapWithGenericValues() {
        AsyncNamedMapFieldsBean bean = ctx.getBean(AsyncNamedMapFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getNamedMap().getName(), is("genericValues"));
    }

    @Test
    void shouldInjectAsyncNamedMapWithGenerics() {
        AsyncNamedMapFieldsBean bean = ctx.getBean(AsyncNamedMapFieldsBean.class);
        assertThat(bean.getGenericCache(), is(notNullValue()));
        assertThat(bean.getGenericCache().getNamedMap().getName(), is("numbers"));
    }

    @Test
    void shouldInjectCachesFromDifferentSessions() {
        DifferentSessionBean bean = ctx.getBean(DifferentSessionBean.class);

        assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
        assertThat(bean.getDefaultCcfNumbers().getName(), is("numbers"));
        assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));
        assertThat(bean.getDefaultCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));
        assertThat(bean.getDefaultCcfAsyncNumbers().getNamedMap(), is(bean.getDefaultCcfNumbers()));

        assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
        assertThat(bean.getSpecificCcfNumbers().getName(), is("numbers"));
        assertThat(bean.getSpecificCcfAsyncNumbers(), is(notNullValue()));
        assertThat(bean.getSpecificCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));
        assertThat(bean.getSpecificCcfAsyncNumbers().getNamedMap(), is(bean.getSpecificCcfNumbers()));

        assertThat(bean.getDefaultCcfNumbers(), is(not(bean.getSpecificCcfNumbers())));
    }

    @Test
    void shouldInjectNamedMapUsingFieldName() {
        NamedMapFieldsBean bean = ctx.getBean(NamedMapFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getName(), is("numbers"));
    }

    @Test
    void shouldInjectNamedMapWithGenericKeys() {
        NamedMapFieldsBean bean = ctx.getBean(NamedMapFieldsBean.class);
        assertThat(bean.getGenericKeys(), is(notNullValue()));
        assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
    }

    @Test
    void shouldInjectNamedMapWithGenericValues() {
        NamedMapFieldsBean bean = ctx.getBean(NamedMapFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getName(), is("genericValues"));
    }

    @Test
    void shouldInjectNamedMapWithGenerics() {
        NamedMapFieldsBean bean = ctx.getBean(NamedMapFieldsBean.class);
        assertThat(bean.getGenericCache(), is(notNullValue()));
        assertThat(bean.getGenericCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedAsyncNamedMap() {
        AsyncNamedMapFieldsBean bean = ctx.getBean(AsyncNamedMapFieldsBean.class);
        assertThat(bean.getNamedMap(), is(notNullValue()));
        assertThat(bean.getNamedMap().getNamedMap().getName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedNamedMap() {
        NamedMapFieldsBean bean = ctx.getBean(NamedMapFieldsBean.class);
        assertThat(bean.getNamedMap(), is(notNullValue()));
        assertThat(bean.getNamedMap().getName(), is("numbers"));
    }

    @Test
    void shouldInjectSuperTypeCacheMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        CacheMap map = bean.getCacheMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedMap())));
    }

    @Test
    void shouldInjectSuperTypeConcurrentMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ConcurrentMap map = bean.getConcurrentMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedMap())));
    }

    @Test
    void shouldInjectSuperTypeInvocableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        InvocableMap map = bean.getInvocableMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedMap())));
    }

    @Test
    void shouldInjectSuperTypeObservableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ObservableMap map = bean.getObservableMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedMap())));
    }

    @Test
    void shouldInjectSuperTypeQueryMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        QueryMap map = bean.getQueryMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedMap())));
    }

    @Test
    void testCtorInjection() {
        CtorBean bean = ctx.getBean(CtorBean.class);

        assertThat(bean.getNumbers(), notNullValue());
        assertThat(bean.getNumbers().getName(), is("numbers"));
        assertThat(bean.getLetters(), notNullValue());
        assertThat(bean.getLetters().getNamedMap().getName(), is("letters"));
    }

    // ----- test beans -----------------------------------------------------

    @Singleton
    static class NamedMapFieldsBean {
        @Inject
        private NamedMap numbers;

        @Inject
        @Name("numbers")
        private NamedMap namedMap;

        @Inject
        @Name("numbers")
        private NamedMap<Integer, String> genericCache;

        @Inject
        private NamedMap<List<String>, String> genericKeys;

        @Inject
        private NamedMap<String, List<String>> genericValues;

        public NamedMap<Integer, String> getGenericCache() {
            return genericCache;
        }

        public NamedMap<List<String>, String> getGenericKeys() {
            return genericKeys;
        }

        public NamedMap<String, List<String>> getGenericValues() {
            return genericValues;
        }

        public NamedMap getNamedMap() {
            return namedMap;
        }

        public NamedMap getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class AsyncNamedMapFieldsBean {
        @Inject
        private AsyncNamedMap numbers;

        @Inject
        @Name("numbers")
        private AsyncNamedMap namedMap;

        @Inject
        @Name("numbers")
        private AsyncNamedMap<Integer, String> genericCache;

        @Inject
        private AsyncNamedMap<List<String>, String> genericKeys;

        @Inject
        private AsyncNamedMap<String, List<String>> genericValues;

        public AsyncNamedMap<Integer, String> getGenericCache() {
            return genericCache;
        }

        public AsyncNamedMap<List<String>, String> getGenericKeys() {
            return genericKeys;
        }

        public AsyncNamedMap<String, List<String>> getGenericValues() {
            return genericValues;
        }

        public AsyncNamedMap getNamedMap() {
            return namedMap;
        }

        public AsyncNamedMap getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class DifferentSessionBean {
        @Inject
        @Name("numbers")
        private NamedMap defaultCcfNumbers;

        @Inject
        @Name("numbers")
        private AsyncNamedMap defaultCcfAsyncNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private NamedMap specificCcfNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private AsyncNamedMap specificCcfAsyncNumbers;

        public AsyncNamedMap getDefaultCcfAsyncNumbers() {
            return defaultCcfAsyncNumbers;
        }

        public NamedMap getDefaultCcfNumbers() {
            return defaultCcfNumbers;
        }

        public AsyncNamedMap getSpecificCcfAsyncNumbers() {
            return specificCcfAsyncNumbers;
        }

        public NamedMap getSpecificCcfNumbers() {
            return specificCcfNumbers;
        }
    }

    @Singleton
    static class CtorBean {

        private final NamedMap<Integer, String> numbers;

        private final AsyncNamedMap<String, String> letters;

        @Inject
        CtorBean(@Name("numbers") NamedMap<Integer, String> numbers,
                 @Name("letters") AsyncNamedMap<String, String> letters) {

            this.numbers = numbers;
            this.letters = letters;
        }

        AsyncNamedMap<String, String> getLetters() {
            return letters;
        }

        NamedMap<Integer, String> getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class SuperTypesBean {
        @Inject
        @Name("numbers")
        private NamedMap<Integer, String> namedMap;

        @Inject
        @Name("numbers")
        private InvocableMap<Integer, String> invocableMap;

        @Inject
        @Name("numbers")
        private ObservableMap<Integer, String> observableMap;

        @Inject
        @Name("numbers")
        private ConcurrentMap<Integer, String> concurrentMap;

        @Inject
        @Name("numbers")
        private QueryMap<Integer, String> queryMap;

        @Inject
        @Name("numbers")
        private CacheMap<Integer, String> cacheMap;

        CacheMap<Integer, String> getCacheMap() {
            return cacheMap;
        }

        ConcurrentMap<Integer, String> getConcurrentMap() {
            return concurrentMap;
        }

        InvocableMap<Integer, String> getInvocableMap() {
            return invocableMap;
        }

        NamedMap<Integer, String> getNamedMap() {
            return namedMap;
        }

        ObservableMap<Integer, String> getObservableMap() {
            return observableMap;
        }

        QueryMap<Integer, String> getQueryMap() {
            return queryMap;
        }
    }
}
