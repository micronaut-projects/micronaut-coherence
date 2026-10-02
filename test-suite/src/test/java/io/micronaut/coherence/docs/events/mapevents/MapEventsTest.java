package io.micronaut.coherence.docs.events.mapevents;

import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import io.micronaut.coherence.annotation.Name;
import io.micronaut.coherence.docs.events.EventsHelper;
import io.micronaut.coherence.docs.model.Order;
import io.micronaut.coherence.docs.model.Person;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "MapEventsTest")
@MicronautTest
class MapEventsTest {

    @Inject
    Session session;

    @Inject
    @Name("Customer")
    Session customerSession;

    @Inject
    @Name("Catalog")
    Session catalogSession;

    @Inject
    PersonController personController;

    @Inject
    MapEventListeners listeners;

    @Inject
    EventTypesListener eventTypesListener;

    @Inject
    FilteredListener filteredListener;

    @Inject
    TransformedListener transformedListener;

    @Test
    void testMapEvents() {
        NamedMap<String, Person> people = session.getMap("people");
        NamedMap<String, String> foo = session.getMap("foo");
        NamedMap<String, String> test = session.getMap("test");
        NamedMap<String, Order> orders = session.getMap("orders");
        NamedMap<String, Order> customerOrders = customerSession.getMap("orders");
        NamedMap<String, Order> catalogOrders = catalogSession.getMap("orders");
        // the listeners are registered asynchronously when the caches are created
        EventsHelper.awaitListeners(people, 2);
        EventsHelper.awaitListeners(foo, 1);
        EventsHelper.awaitListeners(test, 1);
        EventsHelper.awaitListeners(orders, 1);
        EventsHelper.awaitListeners(customerOrders, 1);
        EventsHelper.awaitListeners(catalogOrders, 1);

        people.put("homer", new Person("Homer", "Simpson", 39, "male"));
        people.put("bart", new Person("Bart", "Simpson", 10, "male"));
        foo.put("a", "b");
        test.put("a", "1");
        test.put("a", "2");
        test.remove("a");
        orders.put("1", new Order(1, "homer", "AB1234"));
        customerOrders.put("2", new Order(2, "marge", "AB1234"));
        catalogOrders.put("3", new Order(3, "lisa", "AB1234"));

        // events.adoc / mapEvents.adoc
        await(() -> personController.newPeople.size(), 2);
        await(() -> listeners.getEvents("onFooMapEvent").size(), 1);
        await(() -> listeners.getEvents("onFooCacheEvent").size(), 1);
        await(() -> listeners.getEvents("onStorageFooEvent").size(), 1);
        await(() -> listeners.getEvents("onOrdersEvent").size(), 3);
        await(() -> listeners.getEvents("onCustomerOrdersEvent").size(), 1);
        await(() -> listeners.getEvents("onCustomerEvent").size(), 1);
        await(() -> listeners.getEvents("onCustomerOrders").size(), 1);
        await(() -> listeners.getEvents("onCatalogOrders").size(), 1);
        assertEquals("2", listeners.getEvents("onCustomerOrders").get(0).getKey());
        assertEquals("3", listeners.getEvents("onCatalogOrders").get(0).getKey());
        assertTrue(listeners.getEvents("onEvent").size() >= 9);
        assertTrue(listeners.getEvents("onStorageEvent").size() >= 7);

        // eventTypes.adoc
        await(() -> eventTypesListener.getEvents("onInserted").size(), 1);
        await(() -> eventTypesListener.getEvents("onInsertedOrDeleted").size(), 2);
        await(() -> eventTypesListener.getEvents("onEvent").size(), 3);

        // filteringEvents.adoc
        await(() -> filteredListener.adults.size(), 1);
        assertEquals("homer", filteredListener.adults.get(0).getKey());

        // transformingEvents.adoc
        // the "orders" maps of all sessions are observed
        await(() -> transformedListener.customerIds.size(), 3);
        assertEquals(Set.of("homer", "marge", "lisa"), Set.copyOf(transformedListener.customerIds));
        await(() -> transformedListener.newOrders.size(), 3);
        assertEquals(Map.of(1L, "homer", 2L, "marge", 3L, "lisa"), transformedListener.newOrders);
    }

    private static void await(Supplier<Integer> actual, int expected) {
        long end = System.currentTimeMillis() + 30_000;
        while (!actual.get().equals(expected) && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(expected, actual.get());
    }
}
