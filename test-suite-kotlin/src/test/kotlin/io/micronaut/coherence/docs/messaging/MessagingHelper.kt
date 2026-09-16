package io.micronaut.coherence.docs.messaging

import io.micronaut.context.ApplicationContext

/**
 * A helper for the messaging tests: the subscribers of the `@CoherenceTopicListener` methods are created
 * asynchronously when Coherence starts, so the tests wait for them before publishing messages.
 */
object MessagingHelper {

    fun awaitSubscribed(context: ApplicationContext) {
        val type = Class.forName("io.micronaut.coherence.messaging.CoherenceTopicListenerProcessor")
        val processor = context.getBean(type)
        val method = type.getMethod("isSubscribed").apply { isAccessible = true }
        val end = System.currentTimeMillis() + 60_000
        while (method.invoke(processor) != true) {
            if (System.currentTimeMillis() > end) {
                throw AssertionError("The topic listeners were not subscribed in time")
            }
            Thread.sleep(50)
        }
    }
}
