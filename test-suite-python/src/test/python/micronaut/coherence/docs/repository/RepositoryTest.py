from typing import Annotated

import java
from jakarta.inject import Inject
from micronaut.coherence.examples.model import Book
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .BookRepository import BookRepository


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
