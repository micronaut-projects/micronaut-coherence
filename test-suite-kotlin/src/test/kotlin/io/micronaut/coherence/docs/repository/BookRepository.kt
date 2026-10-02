package io.micronaut.coherence.docs.repository

// tag::imports[]
import com.tangosol.util.UUID
import io.micronaut.coherence.data.annotation.CoherenceRepository
import io.micronaut.coherence.docs.model.Book
import io.micronaut.data.repository.CrudRepository
// end::imports[]

// tag::clazz[]
@CoherenceRepository("book")
interface BookRepository : CrudRepository<Book, UUID> {

    fun findByTitleStartingWith(keyword: String): List<Book>
}
// end::clazz[]
