# TODO(python): the repository cannot be compiled yet: the Java class generated for a Python interface extending
# `CrudRepository[Book, UUID]` with the *Java* entity class `Book` as type argument declares the inherited
# `deleteAll(Iterable<? extends E>)` method as `deleteAll(Iterable<? extends ? extends Book> entities)`, which is
# not valid Java ("illegal start of type"). A Python entity class works, but a Python class cannot be stored in
# Coherence (see DISABLED_TESTS.md), so the example is documented for Java, Kotlin and Groovy only.
#
# tag::imports[]
# from com.tangosol.util import UUID
# from micronaut.coherence.data.annotation import CoherenceRepository
# from micronaut.coherence.examples.model import Book
# from micronaut.data.repository import CrudRepository
# end::imports[]
#
#
# tag::clazz[]
# @CoherenceRepository("book")
# class BookRepository(CrudRepository[Book, UUID]):
#
#     def findByTitleStartingWith(self, keyword: str) -> list[Book]:
#         ...
# end::clazz[]
