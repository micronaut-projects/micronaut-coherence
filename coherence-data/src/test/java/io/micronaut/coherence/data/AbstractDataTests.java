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
import com.tangosol.util.UUID;
import io.micronaut.coherence.data.model.Author;
import io.micronaut.coherence.data.model.Book;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base test class using {@link Book}s for functional validation.
 */
public class AbstractDataTests {
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
     * Book: The Hobbit
     * Author: John Tolkien
     */
    protected static final Book HOBBIT = new Book("The Hobbit", 355, JOHN_TOLKIEN, new GregorianCalendar(1937,
            Calendar.SEPTEMBER, 21, 0, 0));

    /**
     * The {@link NamedMap} the {@code Repository} should be using.
     */
    @Inject
    protected NamedMap<UUID, Book> book;

    /**
     * A {@link Set} of {@link Book books} for validating test results.
     */
    protected Set<Book> books = new LinkedHashSet<>();

    /**
     * Initializes/resets the {@link NamedMap} before each test.
     */
    @BeforeEach
    public void _before() {
        book.clear(); // cache

        books.add(DUNE);
        books.add(DUNE_MESSIAH);
        books.add(NAME_OF_THE_WIND);
        books.add(HOBBIT);

        book.putAll(books.stream().collect(Collectors.toMap(Book::getUuid, b -> b)));
    }
}
