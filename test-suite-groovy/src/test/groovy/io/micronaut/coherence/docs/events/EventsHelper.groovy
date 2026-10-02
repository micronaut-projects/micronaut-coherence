package io.micronaut.coherence.docs.events

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.PartitionedCache
import com.tangosol.coherence.component.util.safeService.SafeCacheService
import com.tangosol.net.NamedMap

/**
 * A helper for the event tests: the {@code @CoherenceEventListener} map listeners are registered
 * asynchronously when a cache is created, so the tests wait for the registration before mutating the caches.
 */
final class EventsHelper {

    private EventsHelper() {
    }

    static void awaitListeners(NamedMap<?, ?> map, int count) {
        long end = System.currentTimeMillis() + 30_000
        while (getListenerCount(map) < count) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("Expected at least $count listeners on map ${map.name} but found ${getListenerCount(map)}")
            }
            Thread.sleep(50)
        }
        // the remaining listeners of the cache are registered by the same asynchronous task
        Thread.sleep(500)
    }

    static int getListenerCount(NamedMap<?, ?> map) {
        def service = map.service
        if (service instanceof SafeCacheService) {
            service = service.runningCacheService
        }
        def storage = service instanceof PartitionedCache ? service.getStorage(map.name) : null
        storage?.listenerMap?.size() ?: 0
    }
}
