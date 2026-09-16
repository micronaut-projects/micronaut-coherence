package io.micronaut.coherence.docs.events.coherenceevents;

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import com.tangosol.util.processor.ExtractorProcessor;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.docs.model.Order;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "CoherenceEventsTest")
@MicronautTest
class CoherenceEventsTest {

    @Inject
    Session session;

    @Inject
    @Name("BackEnd")
    Session backEndSession;

    @Inject
    CacheLifecycleListener cacheLifecycleListener;

    @Inject
    CoherenceLifecycleListener coherenceLifecycleListener;

    @Inject
    EntryListener entryListener;

    @Inject
    EntryProcessorListener entryProcessorListener;

    @Inject
    LifecycleListener lifecycleListener;

    @Inject
    SessionLifecycleListener sessionLifecycleListener;

    @Inject
    TransactionListener transactionListener;

    @Inject
    TransferListener transferListener;

    @Inject
    UnsolicitedCommitListener unsolicitedCommitListener;

    @Test
    void testCoherenceEvents() {
        NamedMap<String, Order> orders = session.getMap("orders");
        NamedMap<String, Person> people = backEndSession.getMap("people");

        orders.put("1", new Order(1, "homer", "AB1234"));
        assertEquals("homer", orders.invoke("1", new ExtractorProcessor<>("getCustomerId")));
        orders.remove("1");
        people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        orders.destroy();

        // cacheLifecycleEvent.adoc
        await(cacheLifecycleListener.events, "onEvent:CREATED:orders", "onEvent:DESTROYED:orders",
                "onCreatedOrDestroyed:CREATED:orders", "onCreatedOrDestroyed:DESTROYED:orders",
                "onOrdersEvent:CREATED:orders", "onStorageServiceEvent:CREATED:orders", "onBackEndEvent:CREATED:people");
        assertFalse(cacheLifecycleListener.events.contains("onOrdersEvent:CREATED:people"));
        assertFalse(cacheLifecycleListener.events.contains("onBackEndEvent:CREATED:orders"));

        // coherenceLifecycleEvent.adoc
        await(coherenceLifecycleListener.events, "onEvent:STARTED", "onStartedOrStopped:STARTED", "onDefaultEvent:STARTED");
        assertFalse(coherenceLifecycleListener.events.contains("onStartedOrStopped:STARTING"));
        assertFalse(coherenceLifecycleListener.events.stream().anyMatch(e -> e.startsWith("onCustomersEvent")));

        // entryEvent.adoc
        await(entryListener.events, "onEvent:INSERTING:1", "onEvent:INSERTED:1", "onEvent:REMOVED:1",
                "onInsertedOrRemoved:INSERTED:1", "onInsertedOrRemoved:REMOVED:1",
                "onOrdersEvent:INSERTED:1", "onStorageServiceEvent:INSERTED:1", "onBackEndEvent:INSERTED:homer");
        assertFalse(entryListener.events.contains("onInsertedOrRemoved:INSERTING:1"));
        assertFalse(entryListener.events.contains("onOrdersEvent:INSERTED:homer"));

        // entryProcessorEvent.adoc
        await(entryProcessorListener.events, "onEvent:EXECUTING:orders", "onEvent:EXECUTED:orders",
                "onExecuted:EXECUTED:orders", "onOrdersEvent:EXECUTED:orders", "onStorageServiceEvent:EXECUTED:orders");
        assertFalse(entryProcessorListener.events.contains("onExecuted:EXECUTING:orders"));
        assertFalse(entryProcessorListener.events.stream().anyMatch(e -> e.startsWith("onBackEndEvent")));

        // lifecycleEvent.adoc
        await(lifecycleListener.events, "onEvent:ACTIVATED", "onActivatedOrDisposing:ACTIVATED");
        assertFalse(lifecycleListener.events.contains("onActivatedOrDisposing:ACTIVATING"));

        // sessionLifecycleEvent.adoc
        await(sessionLifecycleListener.events, "onEvent:STARTED:Customers", "onStartedOrStopped:STARTED:Customers",
                "onCustomersEvent:STARTED:Customers", "onDefaultEvent:STARTED:");
        assertFalse(sessionLifecycleListener.events.contains("onCustomersEvent:STARTED:Catalog"));

        // transactionEvent.adoc
        await(transactionListener.events, "onEvent:COMMITTING", "onEvent:COMMITTED", "onCommitted:COMMITTED",
                "onStorageServiceEvent:COMMITTED");
        assertFalse(transactionListener.events.contains("onCommitted:COMMITTING"));

        // transferEvent.adoc
        await(transferListener.events, "onEvent:ASSIGNED", "onStorageServiceEvent:ASSIGNED");
        assertFalse(transferListener.events.stream().anyMatch(e -> e.startsWith("onLost")));

        // unsolicitedCommitEvent.adoc
        assertNotNull(unsolicitedCommitListener);
        assertTrue(unsolicitedCommitListener.events.isEmpty());
    }

    private static void await(List<String> events, String... expected) {
        long end = System.currentTimeMillis() + 30_000;
        while (!events.containsAll(List.of(expected)) && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (String e : expected) {
            assertTrue(events.contains(e), () -> "Expected event " + e + " in " + events);
        }
    }
}
