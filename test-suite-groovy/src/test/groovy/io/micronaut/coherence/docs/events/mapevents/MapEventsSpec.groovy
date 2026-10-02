package io.micronaut.coherence.docs.events.mapevents

import com.tangosol.net.NamedMap
import com.tangosol.net.Session
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.events.EventsHelper
import io.micronaut.coherence.docs.model.Order
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Property(name = "spec.name", value = "MapEventsTest")
@MicronautTest
class MapEventsSpec extends Specification {

    @Inject
    Session session

    @Inject
    @Name("Customer")
    Session customerSession

    @Inject
    @Name("Catalog")
    Session catalogSession

    @Inject
    PersonController personController

    @Inject
    MapEventListeners listeners

    @Inject
    EventTypesListener eventTypesListener

    @Inject
    FilteredListener filteredListener

    @Inject
    TransformedListener transformedListener

    void "test map events"() {
        given:
        NamedMap<String, Person> people = session.getMap("people")
        NamedMap<String, String> foo = session.getMap("foo")
        NamedMap<String, String> test = session.getMap("test")
        NamedMap<String, Order> orders = session.getMap("orders")
        NamedMap<String, Order> customerOrders = customerSession.getMap("orders")
        NamedMap<String, Order> catalogOrders = catalogSession.getMap("orders")
        // the listeners are registered asynchronously when the caches are created
        EventsHelper.awaitListeners(people, 2)
        EventsHelper.awaitListeners(foo, 1)
        EventsHelper.awaitListeners(test, 1)
        EventsHelper.awaitListeners(orders, 1)
        EventsHelper.awaitListeners(customerOrders, 1)
        EventsHelper.awaitListeners(catalogOrders, 1)
        def conditions = new PollingConditions(timeout: 30)

        when:
        people.put("homer", new Person("Homer", "Simpson", 39, "male"))
        people.put("bart", new Person("Bart", "Simpson", 10, "male"))
        foo.put("a", "b")
        test.put("a", "1")
        test.put("a", "2")
        test.remove("a")
        orders.put("1", new Order(1, "homer", "AB1234"))
        customerOrders.put("2", new Order(2, "marge", "AB1234"))
        catalogOrders.put("3", new Order(3, "lisa", "AB1234"))

        then: "events.adoc / mapEvents.adoc"
        conditions.eventually {
            assert personController.newPeople.size() == 2
            assert listeners.getEvents("onFooMapEvent").size() == 1
            assert listeners.getEvents("onFooCacheEvent").size() == 1
            assert listeners.getEvents("onStorageFooEvent").size() == 1
            assert listeners.getEvents("onOrdersEvent").size() == 3
            assert listeners.getEvents("onCustomerOrdersEvent").size() == 1
            assert listeners.getEvents("onCustomerEvent").size() == 1
            assert listeners.getEvents("onCustomerOrders").size() == 1
            assert listeners.getEvents("onCatalogOrders").size() == 1
        }
        listeners.getEvents("onCustomerOrders")[0].key == "2"
        listeners.getEvents("onCatalogOrders")[0].key == "3"
        listeners.getEvents("onEvent").size() >= 9
        listeners.getEvents("onStorageEvent").size() >= 7

        and: "eventTypes.adoc"
        conditions.eventually {
            assert eventTypesListener.getEvents("onInserted").size() == 1
            assert eventTypesListener.getEvents("onInsertedOrDeleted").size() == 2
            assert eventTypesListener.getEvents("onEvent").size() == 3
        }

        and: "filteringEvents.adoc"
        conditions.eventually {
            assert filteredListener.adults.size() == 1
        }
        filteredListener.adults[0].key == "homer"

        and: "transformingEvents.adoc"
        conditions.eventually {
            // the "orders" maps of all sessions are observed
            assert transformedListener.customerIds as Set == ["homer", "marge", "lisa"] as Set
            assert transformedListener.newOrders == [1L: "homer", 2L: "marge", 3L: "lisa"]
        }
    }
}
