# tag::imports[]
from typing import Annotated

from com.tangosol.util import MapEvent
from jakarta.inject import Singleton
from micronaut.coherence.annotation import CoherenceEventListener, Inserted, MapName, PropertyExtractor
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MapEventsTest")
@Singleton
class TransformedListener:

    def __init__(self):
        self.customer_ids: list[str] = []
        self.new_orders: dict[int, str] = {}

    # tag::extractor[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                                                   # <1>
    def on_order(self, event: Annotated[MapEvent[str, str], MapName("orders")]) -> None:  # <2> <3>
        self.customer_ids.append(event.getNewValue())  # process event...
    # end::extractor[]

    # tag::extractors[]
    @CoherenceEventListener
    @PropertyExtractor("customerId")                     # <1>
    @PropertyExtractor("orderId")
    def on_new_order(self, event: Annotated[MapEvent[str, list[object]],  # <3>
                                            Inserted,                      # <2>
                                            MapName("orders")]) -> None:
        values = event.getNewValue()
        customer_id = values.get(0)                      # <4>
        order_id = values.get(1)
        self.new_orders[order_id] = customer_id
    # end::extractors[]
