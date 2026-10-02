package io.micronaut.coherence.docs.repository;

// tag::imports[]
import com.tangosol.util.UUID;
import io.micronaut.coherence.data.AbstractCoherenceRepository;
import io.micronaut.coherence.data.annotation.CoherenceRepository;
import io.micronaut.coherence.docs.model.Book;
// end::imports[]

// tag::clazz[]
@CoherenceRepository("book")
public abstract class CoherenceBookRepository extends AbstractCoherenceRepository<Book, UUID> {
}
// end::clazz[]
