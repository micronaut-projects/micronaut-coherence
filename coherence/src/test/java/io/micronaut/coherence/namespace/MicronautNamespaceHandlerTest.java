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
package io.micronaut.coherence.namespace;

import javax.inject.Named;
import javax.inject.Singleton;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.net.cache.ReadWriteBackingMap;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link MicronautNamespaceHandler}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@MicronautTest(startApplication = false, propertySources = "classpath:micronaut-namespacehandler-test.yaml")
class MicronautNamespaceHandlerTest {

    private static ExtensibleConfigurableCacheFactory eccf;

    @BeforeAll
    static void setup() {
        String xml = "micronaut-namespace-handler-test-config.xml";
        ExtensibleConfigurableCacheFactory.Dependencies deps
                = ExtensibleConfigurableCacheFactory.DependenciesHelper.newInstance(xml);
        eccf = new ExtensibleConfigurableCacheFactory(deps);
    }

    @AfterAll
    static void cleanup() {
        eccf.dispose();
    }

    @Test
    void shouldInjectBean() {
        Object store = getCacheStore("foo");
        assertThat(store, is(instanceOf(StoreBeanOne.class)));
    }

    @Test
    void shouldInjectOverriddenBean() {
        Object store = getCacheStore("bar");
        assertThat(store, is(instanceOf(StoreBeanTwo.class)));
    }

    Object getCacheStore(String cacheName) {
        NamedCache<Object, Object> cache = eccf.ensureCache(cacheName, null);
        BackingMapManager manager = cache.getCacheService().getBackingMapManager();
        BackingMapManagerContext context = manager.getContext();
        BackingMapContext backingMapContext = context.getBackingMapContext(cache.getCacheName());
        ReadWriteBackingMap backingMap = (ReadWriteBackingMap) backingMapContext.getBackingMap();
        ReadWriteBackingMap.StoreWrapper cacheStore = backingMap.getCacheStore();
        return cacheStore.getStore();
    }

    @Singleton
    @Named("TestStoreOne")
    public static class StoreBeanOne<K, V> implements CacheStore<K, V> {
        @Override
        public void store(K k, V v) {
        }

        @Override
        public void erase(K k) {
        }

        @Override
        public V load(K k) {
            return null;
        }
    }

    @Singleton
    @Named("TestStoreTwo")
    public static class StoreBeanTwo<K, V> implements CacheStore<K, V> {
        @Override
        public void store(K k, V v) {
        }

        @Override
        public void erase(K k) {
        }

        @Override
        public V load(K k) {
            return null;
        }
    }
}
