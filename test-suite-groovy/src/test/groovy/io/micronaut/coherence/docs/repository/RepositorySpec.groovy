package io.micronaut.coherence.docs.repository

import com.tangosol.util.Extractors
import com.tangosol.util.Filters
import io.micronaut.coherence.docs.model.Book
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@MicronautTest
class RepositorySpec extends Specification {

    @Inject
    BookRepository bookRepository

    @Inject
    CoherenceBookRepository coherenceBookRepository

    @Inject
    CoherenceAsyncBookRepository coherenceAsyncBookRepository

    void "test repositories"() {
        when:
        Book book = bookRepository.save(new Book("Micronaut in Action", 400))

        then:
        bookRepository.count() == 1
        bookRepository.findById(book.uuid).present
        bookRepository.findByTitleStartingWith("Micronaut")*.title == ["Micronaut in Action"]

        and: "the Coherence repositories expose the Coherence API, for example filters, on the same book cache"
        coherenceBookRepository.count() == 1
        coherenceBookRepository.getAll(Filters.greater(Extractors.extract("pages"), 100))*.uuid == [book.uuid]
        coherenceAsyncBookRepository.count().get(1, TimeUnit.MINUTES) == 1L

        when:
        bookRepository.deleteAll()

        then:
        coherenceBookRepository.count() == 0
    }
}
