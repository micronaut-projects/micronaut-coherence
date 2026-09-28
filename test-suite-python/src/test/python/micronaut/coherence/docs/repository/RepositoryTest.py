from typing import Annotated

from jakarta.inject import Inject
from micronaut.coherence.examples.model import Book
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .BookRepository import BookRepository


# TODO(python): the CoherenceBookRepository / CoherenceAsyncBookRepository examples (a Python class extending the
# abstract AbstractCoherenceRepository / AbstractCoherenceAsyncRepository) are not ported: the class generated for
# the Python repository bridges the abstract getMapInternal() of the Java base to Python instead of leaving it to the
# implementation Micronaut Data generates ("No Python member [getMapInternal] found"). Re-checked against core 5.2.9.
@MicronautTest
class RepositoryTest:
    book_repository: Annotated[BookRepository, Inject]

    @Test
    def test_repository(self):
        book = self.book_repository.save(Book("Micronaut in Action", 400))
        assert self.book_repository.count() == 1
        assert self.book_repository.findById(book.getUuid()).isPresent()
        assert [b.getTitle() for b in self.book_repository.findByTitleStartingWith("Micronaut")] == ["Micronaut in Action"]

        self.book_repository.deleteAll()
        assert self.book_repository.count() == 0
