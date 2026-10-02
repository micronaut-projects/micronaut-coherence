from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.coherence.annotation import CoherencePublisher, Topic
from micronaut.coherence.examples.model import Book
from org.reactivestreams import Publisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="MessagingTest")
@CoherencePublisher
class BookClient(ABC):

    # tag::mono[]
    @Topic("books")
    @abstractmethod
    def send_book(self, book: Publisher[Book]) -> Publisher[Book]:
        ...
    # end::mono[]

    # tag::flux[]
    @Topic("books")
    @abstractmethod
    def send_books(self, books: Publisher[Book]) -> Publisher[Book]:
        ...
    # end::flux[]
