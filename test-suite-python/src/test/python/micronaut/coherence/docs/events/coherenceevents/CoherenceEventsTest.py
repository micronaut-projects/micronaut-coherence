from time import sleep
from typing import Annotated

from com.tangosol.net import Session
from com.tangosol.util.processor import ExtractorProcessor
from jakarta.inject import Inject
from micronaut.coherence.annotation import Name
from micronaut.coherence.examples.model import Order, Person
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .CacheLifecycleListener import CacheLifecycleListener
from .CoherenceLifecycleListener import CoherenceLifecycleListener
from .EntryListener import EntryListener
from .EntryProcessorListener import EntryProcessorListener
from .LifecycleListener import LifecycleListener
from .SessionLifecycleListener import SessionLifecycleListener
from .TransactionListener import TransactionListener
from .TransferListener import TransferListener
from .UnsolicitedCommitListener import UnsolicitedCommitListener


def await_events(events: list[str], *expected: str) -> None:
    for _ in range(600):
        if all(e in events for e in expected):
            break
        sleep(0.05)
    for e in expected:
        assert e in events, f"Expected event {e} in {events}"


def assert_not_received(events: list[str], event: str) -> None:
    assert event not in events, f"Unexpected event {event} in {events}"


@Property(name="spec.name", value="CoherenceEventsTest")
@MicronautTest
class CoherenceEventsTest:
    session: Annotated[Session, Inject]
    back_end_session: Annotated[Session, Inject, Name("BackEnd")]
    cache_lifecycle_listener: Annotated[CacheLifecycleListener, Inject]
    coherence_lifecycle_listener: Annotated[CoherenceLifecycleListener, Inject]
    entry_listener: Annotated[EntryListener, Inject]
    entry_processor_listener: Annotated[EntryProcessorListener, Inject]
    lifecycle_listener: Annotated[LifecycleListener, Inject]
    session_lifecycle_listener: Annotated[SessionLifecycleListener, Inject]
    transaction_listener: Annotated[TransactionListener, Inject]
    transfer_listener: Annotated[TransferListener, Inject]
    unsolicited_commit_listener: Annotated[UnsolicitedCommitListener, Inject]

    @Test
    def test_coherence_events(self):
        orders = self.session.getMap("orders")
        people = self.back_end_session.getMap("people")

        orders.put("1", Order(1, "homer", "AB1234"))
        assert orders.invoke("1", ExtractorProcessor("getCustomerId")) == "homer"
        orders.remove("1")
        people.put("homer", Person("Homer", "Simpson", 39, "male"))
        orders.destroy()

        # cacheLifecycleEvent.adoc
        events = self.cache_lifecycle_listener.events
        await_events(events, "onEvent:CREATED:orders", "onEvent:DESTROYED:orders",
                     "onCreatedOrDestroyed:CREATED:orders", "onCreatedOrDestroyed:DESTROYED:orders",
                     "onOrdersEvent:CREATED:orders", "onStorageServiceEvent:CREATED:orders", "onBackEndEvent:CREATED:people")
        assert_not_received(events, "onOrdersEvent:CREATED:people")
        assert_not_received(events, "onBackEndEvent:CREATED:orders")

        # coherenceLifecycleEvent.adoc
        events = self.coherence_lifecycle_listener.events
        await_events(events, "onEvent:STARTED", "onStartedOrStopped:STARTED", "onDefaultEvent:STARTED")
        assert_not_received(events, "onStartedOrStopped:STARTING")
        assert not any(e.startswith("onCustomersEvent") for e in events)

        # entryEvent.adoc
        events = self.entry_listener.events
        await_events(events, "onEvent:INSERTING:1", "onEvent:INSERTED:1", "onEvent:REMOVED:1",
                     "onInsertedOrRemoved:INSERTED:1", "onInsertedOrRemoved:REMOVED:1",
                     "onOrdersEvent:INSERTED:1", "onStorageServiceEvent:INSERTED:1", "onBackEndEvent:INSERTED:homer")
        assert_not_received(events, "onInsertedOrRemoved:INSERTING:1")
        assert_not_received(events, "onOrdersEvent:INSERTED:homer")

        # entryProcessorEvent.adoc
        events = self.entry_processor_listener.events
        await_events(events, "onEvent:EXECUTING:orders", "onEvent:EXECUTED:orders",
                     "onExecuted:EXECUTED:orders", "onOrdersEvent:EXECUTED:orders", "onStorageServiceEvent:EXECUTED:orders")
        assert_not_received(events, "onExecuted:EXECUTING:orders")
        assert not any(e.startswith("onBackEndEvent") for e in events)

        # lifecycleEvent.adoc
        events = self.lifecycle_listener.events
        await_events(events, "onEvent:ACTIVATED", "onActivatedOrDisposing:ACTIVATED")
        assert_not_received(events, "onActivatedOrDisposing:ACTIVATING")

        # sessionLifecycleEvent.adoc
        events = self.session_lifecycle_listener.events
        await_events(events, "onEvent:STARTED:Customers", "onStartedOrStopped:STARTED:Customers",
                     "onCustomersEvent:STARTED:Customers", "onDefaultEvent:STARTED:")
        assert_not_received(events, "onCustomersEvent:STARTED:Catalog")

        # transactionEvent.adoc
        events = self.transaction_listener.events
        await_events(events, "onEvent:COMMITTING", "onEvent:COMMITTED", "onCommitted:COMMITTED",
                     "onStorageServiceEvent:COMMITTED")
        assert_not_received(events, "onCommitted:COMMITTING")

        # transferEvent.adoc
        events = self.transfer_listener.events
        await_events(events, "onEvent:ASSIGNED", "onStorageServiceEvent:ASSIGNED")
        assert not any(e.startswith("onLost") for e in events)

        # unsolicitedCommitEvent.adoc
        assert self.unsolicited_commit_listener is not None
        assert self.unsolicited_commit_listener.events == []
