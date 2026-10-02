package io.micronaut.coherence.docs.repository;

import com.tangosol.util.Extractors;
import com.tangosol.util.Filters;
import io.micronaut.coherence.docs.model.Book;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class RepositoryTest {

    @Inject
    BookRepository bookRepository;

    @Inject
    CoherenceBookRepository coherenceBookRepository;

    @Inject
    CoherenceAsyncBookRepository coherenceAsyncBookRepository;

    @Test
    void testRepositories() throws Exception {
        Book book = bookRepository.save(new Book("Micronaut in Action", 400));
        assertEquals(1, bookRepository.count());
        assertTrue(bookRepository.findById(book.getUuid()).isPresent());
        assertEquals(List.of("Micronaut in Action"),
                bookRepository.findByTitleStartingWith("Micronaut").stream().map(Book::getTitle).toList());

        // the Coherence repositories expose the Coherence API, for example filters, on the same "book" cache
        assertEquals(1, coherenceBookRepository.count());
        assertEquals(List.of(book.getUuid()), coherenceBookRepository.getAll(Filters.greater(Extractors.extract("pages"), 100)).stream().map(Book::getUuid).toList());
        assertEquals(1L, coherenceAsyncBookRepository.count().get(1, TimeUnit.MINUTES));

        bookRepository.deleteAll();
        assertEquals(0, coherenceBookRepository.count());
    }
}
