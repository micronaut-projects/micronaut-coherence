/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.coherence.data;

import com.tangosol.net.NamedMap;
import com.tangosol.util.Filters;
import com.tangosol.util.UUID;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.coherence.data.model.Author;
import io.micronaut.coherence.data.model.Book;
import io.micronaut.coherence.data.util.EventRecorder;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.async.AsyncCrudRepository;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Base test class using {@link Book}s for functional validation.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractDataTest {
    /**
     * Author: Frank Herbert
     */
    protected static final Author FRANK_HERBERT = new Author("Frank", "Herbert");

    /**
     * Author: J.R.R. (John) Tolkien.
     */
    protected static final Author JOHN_TOLKIEN = new Author("John", "Tolkien");

    /**
     * Author: Patrick Rothfuss.
     */
    protected static final Author PATRICK_ROTHFUSS = new Author("Patrick", "Rothfuss");

    /**
     * Author: Stephen King.
     */
    protected static final Author STEPHEN_KING = new Author("Stephen", "King");

    /**
     * Book: Dune
     * Author: Frank Herbert
     */
    protected static final Book DUNE = new Book("Dune", 677, FRANK_HERBERT,
            new GregorianCalendar(1964, Calendar.AUGUST, 6, 0, 0));

    /**
     * Book: Dune Messiah
     * Author: Frank Herbert
     */
    protected static final Book DUNE_MESSIAH = new Book("Dune Messiah", 468, FRANK_HERBERT,
            new GregorianCalendar(1967, Calendar.JUNE, 6, 0, 0));

    /**
     * Book: The Name of the Wind
     * Author: Patrick Rothfuss
     */
    protected static final Book NAME_OF_THE_WIND = new Book("The Name of the Wind", 742, PATRICK_ROTHFUSS,
            new GregorianCalendar(2008, Calendar.MARCH, 6, 0, 0));

    /**
     * Book: It
     * Author: Stephen King
     */
    protected static final Book IT = new Book("It", 888, STEPHEN_KING, new GregorianCalendar(1967,
            Calendar.JUNE, 6, 0, 0));

    /**
     * Book: The Hobbit
     * Author: John Tolkien
     */
    protected static final Book HOBBIT = new Book("The Hobbit", 355, JOHN_TOLKIEN, new GregorianCalendar(1937,
            Calendar.SEPTEMBER, 21, 0, 0));

    /**
     * A {@link Set} of {@link Book books} for validating test results.
     */
    protected Set<Book> books;

    /**
     * Backing named map for all tests.
     */
    @Inject
    protected NamedMap<UUID, Book> book;

    /**
     * Event recorder; used by event tests.
     */
    @Inject
    EventRecorder<Book> eventRecorder;

    // ----- test initialization --------------------------------------------

    /**
     * Initializes/resets the {@link NamedMap} before each test.
     */
    @BeforeEach
    public void _before() {
        book.clear(); // NamedMap
        eventRecorder.reset();

        books = new LinkedHashSet<>(4);
        books.add(DUNE);
        books.add(DUNE_MESSIAH);
        books.add(NAME_OF_THE_WIND);
        books.add(HOBBIT);

        book.putAll(books.stream().collect(Collectors.toMap(Book::getUuid, b -> b)));

        // ensure we're not picking up events we're not supposed to
        assertThat(eventRecorder.getRecordedEvents().size(), is(0));
    }

    // ----- helper methods -------------------------------------------------

    /**
     * Wraps an {@link AbstractCoherenceRepository} as a {@link CrudRepository}.
     *
     * @param repo the {@link AbstractCoherenceRepository} to wrap
     *
     * @return the wrapped {@link AbstractCoherenceRepository}
     */
    public CrudRepository asCrudRepo(AbstractCoherenceRepository repo) {
        return new CrudRepository() {
            @NonNull
            @Override
            public Object save(@NonNull final Object entity) {
                return repo.save(entity);
            }

            @NonNull
            @Override
            public Object update(@NonNull final Object entity) {
                return repo.save(entity);
            }

            @NonNull
            @Override
            public Iterable saveAll(@NonNull @Valid @NotNull final Iterable entities) {
                repo.saveAll(StreamSupport.stream(entities.spliterator(), false));
                return entities;
            }

            @NonNull
            @Override
            public Optional findById(@NonNull final Object o) {
                return Optional.of(repo.findById(o));
            }

            @Override
            public boolean existsById(@NonNull final Object o) {
                return repo.getMap().containsKey(o);
            }

            @NonNull
            @Override
            public Iterable findAll() {
                return repo.findAll();
            }

            @Override
            public long count() {
                return repo.count();
            }

            @Override
            public void deleteById(@NonNull final Object o) {
                repo.removeById(o, false);
            }

            @Override
            public void delete(@NonNull final Object entity) {
                repo.remove(entity);
            }

            @Override
            public void deleteAll(@NonNull @NotNull final Iterable entities) {
                repo.removeAll(StreamSupport.stream(entities.spliterator(), false));
            }

            @Override
            public void deleteAll() {
                repo.removeAll(Filters.always());
            }
        };
    }

    /**
     * Wraps an {@link AbstractCoherenceAsyncRepository} as a {@link AsyncCrudRepository}.
     *
     * @param repo the {@link AbstractCoherenceAsyncRepository} to wrap
     *
     * @return the wrapped {@link AbstractCoherenceAsyncRepository}
     */
    public AsyncCrudRepository asAsyncCrudRepo(AbstractCoherenceAsyncRepository repo) {
        return new AsyncCrudRepository() {
            @NonNull
            @Override
            public CompletableFuture save(@NonNull final Object entity) {
                return repo.save(entity);
            }

            @NonNull
            @Override
            public CompletableFuture<? extends Iterable> saveAll(@NonNull @Valid @NotNull final Iterable entities) {
                return repo.saveAll(StreamSupport.stream(entities.spliterator(), false)).thenApply(o -> entities);
            }

            @NonNull
            @Override
            public CompletableFuture findById(@NonNull final Object o) {
                return repo.findById(o);
            }

            @NonNull
            @Override
            public CompletableFuture<Boolean> existsById(@NonNull final Object o) {
                return repo.getMap().containsKey(o);
            }

            @NonNull
            @Override
            public CompletableFuture<? extends Iterable> findAll() {
                return repo.findAll();
            }

            @NonNull
            @Override
            public CompletableFuture<Long> count() {
                return repo.count();
            }

            @NonNull
            @Override
            public CompletableFuture<Void> deleteById(@NonNull final Object o) {
                return repo.removeById(o, false);
            }

            @NonNull
            @Override
            public CompletableFuture<Void> delete(@NonNull final Object entity) {
                return repo.remove(entity).thenApply(unused -> null);
            }

            @NonNull
            @Override
            public CompletableFuture<Void> deleteAll(@NonNull @NotNull final Iterable entities) {
                return repo.removeAll(StreamSupport.stream(entities.spliterator(), false));
            }

            @NonNull
            @Override
            public CompletableFuture<Void> deleteAll() {
                return repo.removeAll(Filters.all());
            }
        };
    }
}
