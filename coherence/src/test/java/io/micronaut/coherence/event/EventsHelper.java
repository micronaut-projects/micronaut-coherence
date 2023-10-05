/*
 * Copyright (c) 2020-2023 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */

package io.micronaut.coherence.event;

import java.util.Map;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.PartitionedCache;
import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.partitionedCache.Storage;
import com.tangosol.coherence.component.util.safeService.SafeCacheService;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedMap;

/**
 * A helper class for event tests.
 */
final class EventsHelper {

    /**
     * Private constructor for utility test.
     */
    private EventsHelper() {
    }

    public static int getListenerCount(NamedMap<?, ?> map) {
        Storage storage = getStorage(map);
        return getListenerCount(storage);
    }

    private static int getListenerCount(Storage storage) {
        if (storage == null) {
            return 0;
        }
        Map<?, ?> map = storage.getListenerMap();
        return map != null ? map.size() : 0;
    }

    private static Storage getStorage(NamedMap<?, ?> map) {
        CacheService service = map.getService();
        if (service instanceof SafeCacheService) {
            service = ((SafeCacheService) service).getRunningCacheService();
        }

        return service instanceof PartitionedCache
                ? ((PartitionedCache) service).getStorage(map.getName())
                : null;
    }
}
