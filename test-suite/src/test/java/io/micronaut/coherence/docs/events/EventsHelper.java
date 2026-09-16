package io.micronaut.coherence.docs.events;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.PartitionedCache;
import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.partitionedCache.Storage;
import com.tangosol.coherence.component.util.safeService.SafeCacheService;
import com.tangosol.net.CacheService;
import com.tangosol.net.NamedMap;

import java.util.Map;

/**
 * A helper for the event tests: the {@code @CoherenceEventListener} map listeners are registered
 * asynchronously when a cache is created, so the tests wait for the registration before mutating the caches.
 */
public final class EventsHelper {

    private EventsHelper() {
    }

    /**
     * Wait until at least {@code count} listener registrations exist for the specified map.
     *
     * @param map   the map to check
     * @param count the minimum number of listener registrations expected
     */
    public static void awaitListeners(NamedMap<?, ?> map, int count) {
        long end = System.currentTimeMillis() + 30_000;
        while (getListenerCount(map) < count) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("Expected at least " + count + " listeners on map " + map.getName()
                        + " but found " + getListenerCount(map));
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        // the remaining listeners of the cache are registered by the same asynchronous task
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static int getListenerCount(NamedMap<?, ?> map) {
        CacheService service = map.getService();
        if (service instanceof SafeCacheService safeCacheService) {
            service = safeCacheService.getRunningCacheService();
        }
        Storage storage = service instanceof PartitionedCache partitionedCache
                ? partitionedCache.getStorage(map.getName())
                : null;
        if (storage == null) {
            return 0;
        }
        Map<?, ?> listeners = storage.getListenerMap();
        return listeners != null ? listeners.size() : 0;
    }
}
