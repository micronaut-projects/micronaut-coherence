package io.micronaut.coherence.docs.model

import com.tangosol.util.UUID
import io.micronaut.core.annotation.Creator
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.io.Serializable

/**
 * A book entity stored by the Micronaut Data repositories of the examples.
 */
@MappedEntity
class Book @Creator constructor(
    @field:Id val uuid: UUID,
    val title: String,
    val pages: Int
) : Serializable {
    constructor(title: String, pages: Int) : this(UUID(), title, pages)
}
