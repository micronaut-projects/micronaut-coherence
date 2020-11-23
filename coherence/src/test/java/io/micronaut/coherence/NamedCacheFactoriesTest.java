/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package io.micronaut.coherence;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;

import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.NamedCache;
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
class NamedCacheFactoriesTest {

    @Inject 
    ApplicationContext ctx;

    @Test
    void shouldInjectAsyncNamedCacheUsingFieldName() {
        AsyncNamedCacheFieldsBean bean = ctx.getBean(AsyncNamedCacheFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getNamedCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectAsyncNamedCacheWithGenericKeys() {
        AsyncNamedCacheFieldsBean bean = ctx.getBean(AsyncNamedCacheFieldsBean.class);
        assertThat(bean.getGenericKeys(), is(notNullValue()));
        assertThat(bean.getGenericKeys().getNamedCache().getName(), is("genericKeys"));
    }

    @Test
    void shouldInjectAsyncNamedCacheWithGenericValues() {
        AsyncNamedCacheFieldsBean bean = ctx.getBean(AsyncNamedCacheFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getNamedCache().getName(), is("genericValues"));
    }

    @Test
    void shouldInjectAsyncNamedCacheWithGenerics() {
        AsyncNamedCacheFieldsBean bean = ctx.getBean(AsyncNamedCacheFieldsBean.class);
        assertThat(bean.getGenericCache(), is(notNullValue()));
        assertThat(bean.getGenericCache().getNamedCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectCachesFromDifferentSessions() {
        DifferentSessionBean bean = ctx.getBean(DifferentSessionBean.class);

        assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
        assertThat(bean.getDefaultCcfNumbers().getName(), is("numbers"));
        assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));
        assertThat(bean.getDefaultCcfAsyncNumbers().getNamedCache().getName(), is("numbers"));
        assertThat(bean.getDefaultCcfAsyncNumbers().getNamedCache(), is(bean.getDefaultCcfNumbers()));

        assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
        assertThat(bean.getSpecificCcfNumbers().getName(), is("numbers"));
        assertThat(bean.getSpecificCcfAsyncNumbers(), is(notNullValue()));
        assertThat(bean.getSpecificCcfAsyncNumbers().getNamedCache().getName(), is("numbers"));
        assertThat(bean.getSpecificCcfAsyncNumbers().getNamedCache(), is(bean.getSpecificCcfNumbers()));

        assertThat(bean.getDefaultCcfNumbers(), is(not(bean.getSpecificCcfNumbers())));
    }

    @Test
    void shouldInjectNamedCacheUsingFieldName() {
        NamedCacheFieldsBean bean = ctx.getBean(NamedCacheFieldsBean.class);
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getName(), is("numbers"));
    }

    @Test
    void shouldInjectNamedCacheWithGenericKeys() {
        NamedCacheFieldsBean bean = ctx.getBean(NamedCacheFieldsBean.class);
        assertThat(bean.getGenericKeys(), is(notNullValue()));
        assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
    }

    @Test
    void shouldInjectNamedCacheWithGenericValues() {
        NamedCacheFieldsBean bean = ctx.getBean(NamedCacheFieldsBean.class);
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getName(), is("genericValues"));
    }

    @Test
    void shouldInjectNamedCacheWithGenerics() {
        NamedCacheFieldsBean bean = ctx.getBean(NamedCacheFieldsBean.class);
        assertThat(bean.getGenericCache(), is(notNullValue()));
        assertThat(bean.getGenericCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedAsyncNamedCache() {
        AsyncNamedCacheFieldsBean bean = ctx.getBean(AsyncNamedCacheFieldsBean.class);
        assertThat(bean.getNamedCache(), is(notNullValue()));
        assertThat(bean.getNamedCache().getNamedCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectQualifiedNamedCache() {
        NamedCacheFieldsBean bean = ctx.getBean(NamedCacheFieldsBean.class);
        assertThat(bean.getNamedCache(), is(notNullValue()));
        assertThat(bean.getNamedCache().getName(), is("numbers"));
    }

    @Test
    void shouldInjectSuperTypeCacheMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        CacheMap map = bean.getCacheMap();
        NamedCache cache = bean.getNamedCache();
        assertThat(map, is(notNullValue()));
        assertThat(cache, is(notNullValue()));
        assertThat(map, is(sameInstance(cache)));
    }

    @Test
    void shouldInjectSuperTypeConcurrentMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ConcurrentMap map = bean.getConcurrentMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedCache())));
    }

    @Test
    void shouldInjectSuperTypeInvocableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        InvocableMap map = bean.getInvocableMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedCache())));
    }

    @Test
    void shouldInjectSuperTypeObservableMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        ObservableMap map = bean.getObservableMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedCache())));
    }

    @Test
    void shouldInjectSuperTypeQueryMap() {
        SuperTypesBean bean = ctx.getBean(SuperTypesBean.class);
        QueryMap map = bean.getQueryMap();
        assertThat(map, is(notNullValue()));
        assertThat(map, is(sameInstance(bean.getNamedCache())));
    }

    @Test
    void testCtorInjection() {
        CtorBean bean = ctx.getBean(CtorBean.class);

        assertThat(bean.getNumbers(), notNullValue());
        assertThat(bean.getNumbers().getName(), is("numbers"));
        assertThat(bean.getLetters(), notNullValue());
        assertThat(bean.getLetters().getNamedCache().getName(), is("letters"));
    }

    // ----- test beans -----------------------------------------------------

    @Singleton
    static class NamedCacheFieldsBean {
        @Inject
        private NamedCache numbers;

        @Inject
        @Name("numbers")
        private NamedCache namedCache;

        @Inject
        @Name("numbers")
        private NamedCache<Integer, String> genericCache;

        @Inject
        private NamedCache<List<String>, String> genericKeys;

        @Inject
        private NamedCache<String, List<String>> genericValues;

        public NamedCache<Integer, String> getGenericCache() {
            return genericCache;
        }

        public NamedCache<List<String>, String> getGenericKeys() {
            return genericKeys;
        }

        public NamedCache<String, List<String>> getGenericValues() {
            return genericValues;
        }

        public NamedCache getNamedCache() {
            return namedCache;
        }

        public NamedCache getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class AsyncNamedCacheFieldsBean {
        @Inject
        private AsyncNamedCache numbers;

        @Inject
        @Name("numbers")
        private AsyncNamedCache namedCache;

        @Inject
        @Name("numbers")
        private AsyncNamedCache<Integer, String> genericCache;

        @Inject
        private AsyncNamedCache<List<String>, String> genericKeys;

        @Inject
        private AsyncNamedCache<String, List<String>> genericValues;

        public AsyncNamedCache<Integer, String> getGenericCache() {
            return genericCache;
        }

        public AsyncNamedCache<List<String>, String> getGenericKeys() {
            return genericKeys;
        }

        public AsyncNamedCache<String, List<String>> getGenericValues() {
            return genericValues;
        }

        public AsyncNamedCache getNamedCache() {
            return namedCache;
        }

        public AsyncNamedCache getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class DifferentSessionBean {
        @Inject
        @Name("numbers")
        private NamedCache defaultCcfNumbers;

        @Inject
        @Name("numbers")
        private AsyncNamedCache defaultCcfAsyncNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private NamedCache specificCcfNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test")
        private AsyncNamedCache specificCcfAsyncNumbers;

        public AsyncNamedCache getDefaultCcfAsyncNumbers() {
            return defaultCcfAsyncNumbers;
        }

        public NamedCache getDefaultCcfNumbers() {
            return defaultCcfNumbers;
        }

        public AsyncNamedCache getSpecificCcfAsyncNumbers() {
            return specificCcfAsyncNumbers;
        }

        public NamedCache getSpecificCcfNumbers() {
            return specificCcfNumbers;
        }
    }

    @Singleton
    static class CtorBean {

        private final NamedCache<Integer, String> numbers;

        private final AsyncNamedCache<String, String> letters;

        @Inject
        CtorBean(@Name("numbers") NamedCache<Integer, String> numbers,
                 @Name("letters") AsyncNamedCache<String, String> letters) {

            this.numbers = numbers;
            this.letters = letters;
        }

        AsyncNamedCache<String, String> getLetters() {
            return letters;
        }

        NamedCache<Integer, String> getNumbers() {
            return numbers;
        }
    }

    @Singleton
    static class SuperTypesBean {
        @Inject
        @Name("numbers")
        private NamedCache<Integer, String> namedCache;

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

        NamedCache<Integer, String> getNamedCache() {
            return namedCache;
        }

        ObservableMap<Integer, String> getObservableMap() {
            return observableMap;
        }

        QueryMap<Integer, String> getQueryMap() {
            return queryMap;
        }
    }
}
