package io.micronaut.coherence.docs.messaging;

import io.micronaut.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * A helper for the messaging tests: the subscribers of the {@code @CoherenceTopicListener} methods are created
 * asynchronously when Coherence starts, so the tests wait for them before publishing messages.
 */
public final class MessagingHelper {

    private MessagingHelper() {
    }

    public static void awaitSubscribed(ApplicationContext context) {
        try {
            Class<?> type = Class.forName("io.micronaut.coherence.messaging.CoherenceTopicListenerProcessor");
            Object processor = context.getBean(type);
            Method method = type.getMethod("isSubscribed");
            method.setAccessible(true);
            long end = System.currentTimeMillis() + 60_000;
            while (!Boolean.TRUE.equals(method.invoke(processor))) {
                if (System.currentTimeMillis() > end) {
                    throw new AssertionError("The topic listeners were not subscribed in time");
                }
                Thread.sleep(50);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
