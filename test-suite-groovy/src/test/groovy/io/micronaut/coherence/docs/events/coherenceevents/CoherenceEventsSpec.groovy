package io.micronaut.coherence.docs.events.coherenceevents

import com.tangosol.net.NamedMap
import com.tangosol.net.Session
import com.tangosol.util.processor.ExtractorProcessor
import io.micronaut.coherence.annotation.Name
import io.micronaut.coherence.docs.model.Order
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Property(name = "spec.name", value = "CoherenceEventsTest")
@MicronautTest
class CoherenceEventsSpec extends Specification {

    @Inject
    Session session

    @Inject
    @Name("BackEnd")
    Session backEndSession

    @Inject
    CacheLifecycleListener cacheLifecycleListener

    @Inject
    CoherenceLifecycleListener coherenceLifecycleListener

    @Inject
    EntryListener entryListener

    @Inject
    EntryProcessorListener entryProcessorListener

    @Inject
    LifecycleListener lifecycleListener

    @Inject
    SessionLifecycleListener sessionLifecycleListener

    @Inject
    TransactionListener transactionListener

    @Inject
    TransferListener transferListener

    @Inject
    UnsolicitedCommitListener unsolicitedCommitListener

    void "test coherence events"() {
        given:
        NamedMap<String, Order> orders = session.getMap("orders")
        NamedMap<String, Person> people = backEndSession.getMap("people")
        def conditions = new PollingConditions(timeout: 30)

        when:
        orders.put("1", new Order(1, "homer", "AB1234"))
        def customerId = orders.invoke("1", new ExtractorProcessor<>("getCustomerId"))
        orders.remove("1")
        people.put("homer", new Person("Homer", "Simpson", 39, "male"))
        orders.destroy()

        then:
        customerId == "homer"

        and: "cacheLifecycleEvent.adoc"
        conditions.eventually {
            assert cacheLifecycleListener.events.containsAll(["onEvent:CREATED:orders", "onEvent:DESTROYED:orders",
                    "onCreatedOrDestroyed:CREATED:orders", "onCreatedOrDestroyed:DESTROYED:orders",
                    "onOrdersEvent:CREATED:orders", "onStorageServiceEvent:CREATED:orders", "onBackEndEvent:CREATED:people"])
        }
        !cacheLifecycleListener.events.contains("onOrdersEvent:CREATED:people")
        !cacheLifecycleListener.events.contains("onBackEndEvent:CREATED:orders")

        and: "coherenceLifecycleEvent.adoc"
        conditions.eventually {
            assert coherenceLifecycleListener.events.containsAll(["onEvent:STARTED", "onStartedOrStopped:STARTED", "onDefaultEvent:STARTED"])
        }
        !coherenceLifecycleListener.events.contains("onStartedOrStopped:STARTING")
        !coherenceLifecycleListener.events.any { it.startsWith("onCustomersEvent") }

        and: "entryEvent.adoc"
        conditions.eventually {
            assert entryListener.events.containsAll(["onEvent:INSERTING:1", "onEvent:INSERTED:1", "onEvent:REMOVED:1",
                    "onInsertedOrRemoved:INSERTED:1", "onInsertedOrRemoved:REMOVED:1",
                    "onOrdersEvent:INSERTED:1", "onStorageServiceEvent:INSERTED:1", "onBackEndEvent:INSERTED:homer"])
        }
        !entryListener.events.contains("onInsertedOrRemoved:INSERTING:1")
        !entryListener.events.contains("onOrdersEvent:INSERTED:homer")

        and: "entryProcessorEvent.adoc"
        conditions.eventually {
            assert entryProcessorListener.events.containsAll(["onEvent:EXECUTING:orders", "onEvent:EXECUTED:orders",
                    "onExecuted:EXECUTED:orders", "onOrdersEvent:EXECUTED:orders", "onStorageServiceEvent:EXECUTED:orders"])
        }
        !entryProcessorListener.events.contains("onExecuted:EXECUTING:orders")
        !entryProcessorListener.events.any { it.startsWith("onBackEndEvent") }

        and: "lifecycleEvent.adoc"
        conditions.eventually {
            assert lifecycleListener.events.containsAll(["onEvent:ACTIVATED", "onActivatedOrDisposing:ACTIVATED"])
        }
        !lifecycleListener.events.contains("onActivatedOrDisposing:ACTIVATING")

        and: "sessionLifecycleEvent.adoc"
        conditions.eventually {
            assert sessionLifecycleListener.events.containsAll(["onEvent:STARTED:Customers", "onStartedOrStopped:STARTED:Customers",
                    "onCustomersEvent:STARTED:Customers", "onDefaultEvent:STARTED:"])
        }
        !sessionLifecycleListener.events.contains("onCustomersEvent:STARTED:Catalog")

        and: "transactionEvent.adoc"
        conditions.eventually {
            assert transactionListener.events.containsAll(["onEvent:COMMITTING", "onEvent:COMMITTED", "onCommitted:COMMITTED",
                    "onStorageServiceEvent:COMMITTED"])
        }
        !transactionListener.events.contains("onCommitted:COMMITTING")

        and: "transferEvent.adoc"
        conditions.eventually {
            assert transferListener.events.containsAll(["onEvent:ASSIGNED", "onStorageServiceEvent:ASSIGNED"])
        }
        !transferListener.events.any { it.startsWith("onLost") }

        and: "unsolicitedCommitEvent.adoc"
        unsolicitedCommitListener != null
        unsolicitedCommitListener.events.isEmpty()
    }
}
