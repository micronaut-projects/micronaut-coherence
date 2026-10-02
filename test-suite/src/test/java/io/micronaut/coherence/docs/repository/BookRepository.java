package io.micronaut.coherence.docs.repository;

// tag::imports[]
import com.tangosol.util.UUID;
import io.micronaut.coherence.data.annotation.CoherenceRepository;
import io.micronaut.coherence.docs.model.Book;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
// end::imports[]

// tag::clazz[]
@CoherenceRepository("book")
public interface BookRepository extends CrudRepository<Book, UUID> {

    List<Book> findByTitleStartingWith(String keyword);
}
// end::clazz[]
