package io.micronaut.coherence.docs.events.mapevents

import com.tangosol.net.NamedMap
import com.tangosol.net.Session
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.events.EventsHelper
import io.micronaut.coherence.docs.model.Order
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "MapEventsTest")
@MicronautTest
class MapEventsTest {

    @Inject
    lateinit var session: Session

    @Inject
    @Name("Customer")
    lateinit var customerSession: Session

    @Inject
    @Name("Catalog")
    lateinit var catalogSession: Session

    @Inject
    lateinit var personController: PersonController

    @Inject
    lateinit var listeners: MapEventListeners

    @Inject
    lateinit var eventTypesListener: EventTypesListener

    @Inject
    lateinit var filteredListener: FilteredListener

    @Inject
    lateinit var transformedListener: TransformedListener

    @Test
    fun testMapEvents() {
        val people: NamedMap<String, Person> = session.getMap("people")
        val foo: NamedMap<String, String> = session.getMap("foo")
        val test: NamedMap<String, String> = session.getMap("test")
        val orders: NamedMap<String, Order> = session.getMap("orders")
        val customerOrders: NamedMap<String, Order> = customerSession.getMap("orders")
        val catalogOrders: NamedMap<String, Order> = catalogSession.getMap("orders")
        // the listeners are registered asynchronously when the caches are created
        EventsHelper.awaitListeners(people, 2)
        EventsHelper.awaitListeners(foo, 1)
        EventsHelper.awaitListeners(test, 1)
        EventsHelper.awaitListeners(orders, 1)
        EventsHelper.awaitListeners(customerOrders, 1)
        EventsHelper.awaitListeners(catalogOrders, 1)

        people["homer"] = Person("Homer", "Simpson", 39, "male")
        people["bart"] = Person("Bart", "Simpson", 10, "male")
        foo["a"] = "b"
        test["a"] = "1"
        test["a"] = "2"
        test.remove("a")
        orders["1"] = Order(1, "homer", "AB1234")
        customerOrders["2"] = Order(2, "marge", "AB1234")
        catalogOrders["3"] = Order(3, "lisa", "AB1234")

        // events.adoc / mapEvents.adoc
        await({ personController.newPeople.size }, 2)
        await({ listeners.getEvents("onFooMapEvent").size }, 1)
        await({ listeners.getEvents("onFooCacheEvent").size }, 1)
        await({ listeners.getEvents("onStorageFooEvent").size }, 1)
        await({ listeners.getEvents("onOrdersEvent").size }, 3)
        await({ listeners.getEvents("onCustomerOrdersEvent").size }, 1)
        await({ listeners.getEvents("onCustomerEvent").size }, 1)
        await({ listeners.getEvents("onCustomerOrders").size }, 1)
        await({ listeners.getEvents("onCatalogOrders").size }, 1)
        assertEquals("2", listeners.getEvents("onCustomerOrders")[0].key)
        assertEquals("3", listeners.getEvents("onCatalogOrders")[0].key)
        assertTrue(listeners.getEvents("onEvent").size >= 9)
        assertTrue(listeners.getEvents("onStorageEvent").size >= 7)

        // eventTypes.adoc
        await({ eventTypesListener.getEvents("onInserted").size }, 1)
        await({ eventTypesListener.getEvents("onInsertedOrDeleted").size }, 2)
        await({ eventTypesListener.getEvents("onEvent").size }, 3)

        // filteringEvents.adoc
        await({ filteredListener.adults.size }, 1)
        assertEquals("homer", filteredListener.adults[0].key)

        // transformingEvents.adoc
        // the "orders" maps of all sessions are observed
        await({ transformedListener.customerIds.size }, 3)
        assertEquals(setOf("homer", "marge", "lisa"), transformedListener.customerIds.toSet())
        await({ transformedListener.newOrders.size }, 3)
        assertEquals(mapOf(1L to "homer", 2L to "marge", 3L to "lisa"), transformedListener.newOrders)
    }

    private fun await(actual: () -> Int, expected: Int) {
        val end = System.currentTimeMillis() + 30_000
        while (actual() != expected && System.currentTimeMillis() < end) {
            Thread.sleep(50)
        }
        assertEquals(expected, actual())
    }
}
