package io.micronaut.coherence.docs.repository

// tag::imports[]
import com.tangosol.util.UUID
import io.micronaut.coherence.data.AbstractCoherenceAsyncRepository
import io.micronaut.coherence.data.annotation.CoherenceRepository
import io.micronaut.coherence.docs.model.Book
// end::imports[]

// tag::clazz[]
@CoherenceRepository("book")
abstract class CoherenceAsyncBookRepository : AbstractCoherenceAsyncRepository<Book, UUID>()
// end::clazz[]
