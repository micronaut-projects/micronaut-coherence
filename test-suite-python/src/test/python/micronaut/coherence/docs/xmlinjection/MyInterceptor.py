# tag::imports[]
from com.tangosol.net.events import EventInterceptor
from com.tangosol.net.events.annotation import EntryEvents, Interceptor
from com.tangosol.net.events.partition.cache import EntryEvent
from jakarta.inject import Named, Singleton
# end::imports[]


# tag::clazz[]
@Singleton
@Named("Foo")   # <1>
@Interceptor
@EntryEvents([EntryEvent.Type.INSERTED, EntryEvent.Type.UPDATED, EntryEvent.Type.REMOVED])
class MyInterceptor(EventInterceptor[EntryEvent]):

    def __init__(self):
        self.events: list[str] = []

    def onEvent(self, event: EntryEvent) -> None:
        self.events.append(f"{event.getType()}:{event.getKey()}")  # process the event.
# end::clazz[]
