package io.micronaut.coherence.docs.messaging;

// tag::imports[]
import io.micronaut.coherence.annotation.CoherencePublisher;
import io.micronaut.coherence.annotation.Topic;
import io.micronaut.coherence.docs.model.Book;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "MessagingTest")
@CoherencePublisher
public interface BookClient {

    // tag::mono[]
    @Topic("books")
    Mono<Book> sendBook(Mono<Book> book);
    // end::mono[]

    // tag::flux[]
    @Topic("books")
    Flux<Book> sendBooks(Flux<Book> books);
    // end::flux[]
}
