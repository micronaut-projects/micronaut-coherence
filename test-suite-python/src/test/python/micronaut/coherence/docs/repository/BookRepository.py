from abc import ABC

# tag::imports[]
from com.tangosol.util import UUID
from micronaut.coherence.data.annotation import CoherenceRepository
from micronaut.coherence.examples.model import Book
from micronaut.data.repository import CrudRepository
# end::imports[]


# tag::clazz[]
@CoherenceRepository("book")
class BookRepository(CrudRepository[Book, UUID], ABC):

    def findByTitleStartingWith(self, keyword: str) -> list[Book]:
        ...
# end::clazz[]
