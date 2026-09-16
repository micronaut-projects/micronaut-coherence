package io.micronaut.coherence.docs.messaging

// tag::imports[]
import io.micronaut.coherence.annotation.CoherencePublisher
import io.micronaut.coherence.annotation.Topic
import io.micronaut.coherence.docs.model.Book
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "MessagingTest")
@CoherencePublisher
interface BookClient {

    // tag::mono[]
    @Topic("books")
    fun sendBook(book: Mono<Book>): Mono<Book>
    // end::mono[]

    // tag::flux[]
    @Topic("books")
    fun sendBooks(books: Flux<Book>): Flux<Book>
    // end::flux[]
}
