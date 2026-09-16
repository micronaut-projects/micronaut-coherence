package io.micronaut.coherence.docs.model

import com.tangosol.util.UUID
import io.micronaut.core.annotation.Creator
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

/**
 * A book entity stored by the Micronaut Data repositories of the examples.
 */
@MappedEntity
class Book implements Serializable {

    @Id
    final UUID uuid
    final String title
    final int pages

    @Creator
    Book(UUID uuid, String title, int pages) {
        this.uuid = uuid
        this.title = title
        this.pages = pages
    }

    Book(String title, int pages) {
        this(new UUID(), title, pages)
    }
}
