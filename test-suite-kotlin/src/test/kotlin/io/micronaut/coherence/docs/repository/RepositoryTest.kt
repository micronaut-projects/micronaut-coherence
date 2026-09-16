package io.micronaut.coherence.docs.repository

import com.tangosol.util.Extractors
import com.tangosol.util.Filters
import io.micronaut.coherence.docs.model.Book
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@MicronautTest
class RepositoryTest {

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var coherenceBookRepository: CoherenceBookRepository

    @Inject
    lateinit var coherenceAsyncBookRepository: CoherenceAsyncBookRepository

    @Test
    fun testRepositories() {
        val book = bookRepository.save(Book("Micronaut in Action", 400))
        assertEquals(1, bookRepository.count())
        assertTrue(bookRepository.findById(book.uuid).isPresent)
        assertEquals(listOf("Micronaut in Action"), bookRepository.findByTitleStartingWith("Micronaut").map { it.title })

        // the Coherence repositories expose the Coherence API, for example filters, on the same "book" cache
        assertEquals(1, coherenceBookRepository.count())
        assertEquals(listOf(book.uuid), coherenceBookRepository.getAll(Filters.greater(Extractors.extract<Book, Int>("pages"), 100)).map { it.uuid })
        assertEquals(1L, coherenceAsyncBookRepository.count().get(1, TimeUnit.MINUTES))

        bookRepository.deleteAll()
        assertEquals(0, coherenceBookRepository.count())
    }
}
