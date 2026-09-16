package io.micronaut.coherence.docs.events

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.partitionedService.PartitionedCache
import com.tangosol.coherence.component.util.safeService.SafeCacheService
import com.tangosol.net.NamedMap

/**
 * A helper for the event tests: the `@CoherenceEventListener` map listeners are registered
 * asynchronously when a cache is created, so the tests wait for the registration before mutating the caches.
 */
object EventsHelper {

    fun awaitListeners(map: NamedMap<*, *>, count: Int) {
        val end = System.currentTimeMillis() + 30_000
        while (getListenerCount(map) < count) {
            if (System.currentTimeMillis() > end) {
                throw AssertionError("Expected at least $count listeners on map ${map.name} but found ${getListenerCount(map)}")
            }
            Thread.sleep(50)
        }
        // the remaining listeners of the cache are registered by the same asynchronous task
        Thread.sleep(500)
    }

    fun getListenerCount(map: NamedMap<*, *>): Int {
        var service = map.service
        if (service is SafeCacheService) {
            service = service.runningCacheService
        }
        val storage = (service as? PartitionedCache)?.getStorage(map.name) ?: return 0
        return storage.listenerMap?.size ?: 0
    }
}
