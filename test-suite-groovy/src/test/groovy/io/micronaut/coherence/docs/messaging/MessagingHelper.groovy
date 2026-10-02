package io.micronaut.coherence.docs.messaging

import io.micronaut.context.ApplicationContext

/**
 * A helper for the messaging tests: the subscribers of the {@code @CoherenceTopicListener} methods are created
 * asynchronously when Coherence starts, so the tests wait for them before publishing messages.
 */
final class MessagingHelper {

    private MessagingHelper() {
    }

    static void awaitSubscribed(ApplicationContext context) {
        Class<?> type = Class.forName("io.micronaut.coherence.messaging.CoherenceTopicListenerProcessor")
        Object processor = context.getBean(type)
        def method = type.getMethod("isSubscribed")
        method.accessible = true
        long end = System.currentTimeMillis() + 60_000
        while (method.invoke(processor) != true) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("The topic listeners were not subscribed in time")
            }
            Thread.sleep(50)
        }
    }
}
