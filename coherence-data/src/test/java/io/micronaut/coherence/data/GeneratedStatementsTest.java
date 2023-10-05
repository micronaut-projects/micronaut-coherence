/*
 * Copyright 2017-2023 original authors
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

import com.tangosol.util.UUID;
import io.micronaut.coherence.data.model.Author;
import io.micronaut.coherence.data.model.Book;
import io.micronaut.coherence.data.repositories.BookRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

@MicronautTest(propertySources = {"classpath:sessions.yaml"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeneratedStatementsTest extends AbstractDataTest {
    /**
     * A {@code repository} for validating generated queries.
     */
    @Inject
    protected BookRepository repo;

    // ----- test methods ---------------------------------------------------

    /**
     * Validate it is possible to query books by id.
     */
    @Test
    public void shouldGetBooksById() {
        for (Book b : books) {
            Optional<Book> book = repo.findById(b.getUuid());
            assertThat(book.isPresent(), is(true));
            assertThat(book.get(), is(b));
        }
    }

    /**
     * Validate it is possible to obtain all books associated with a specific {@link Author author}.
     */
    @Test
    public void shouldGetBooksByAuthor() {
        assertThat(repo.findByAuthor(FRANK_HERBERT), containsInAnyOrder(
                books.stream().filter(book -> book.getAuthor().equals(FRANK_HERBERT)).toArray()));
    }

    /**
     * Validate it is possible to find books with pages greater or equal to some value.
     */
    @Test
    public void shouldGetBooksWithPagesGreaterOrEqualTo() {
        assertThat(repo.findByPagesGreaterThanEquals(468), containsInAnyOrder(
                books.stream().filter(book -> book.getPages() >= 468).toArray()));
    }

    /**
     * Validate it is possible to find books with pages less or equal to some value.
     */
    @Test
    public void shouldGetBooksWithPagesLessOrEqualTo() {
        assertThat(repo.findByPagesLessThanEquals(677), containsInAnyOrder(
                books.stream().filter(book -> book.getPages() <= 677).toArray()));
    }

    /**
     * Validate it is possible to find books using {@code like}.
     */
    @Test
    public void shouldGetBooksWithTitleLike() {
        assertThat(repo.findByTitleLike("%Dune%"), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().contains("Dune")).toArray()));
    }

    /**
     * Validate returns {@code true} for {@link Book books} were authored by a known {@link Author author}.
     */
    @Test
    public void shouldReturnTrueForValidAuthor() {
        assertThat(repo.existsByAuthor(FRANK_HERBERT), is(true));
    }

    /**
     * Validate returns {@code false} for an {@link Author author}.
     */
    @Test
    public void shouldReturnFalseForInvalidAuthor() {
        assertThat(repo.existsByAuthor(STEPHEN_KING), is(false));
    }

    /**
     * Validate the expected result is returned when querying for {@link Book books} {@code before} a specific year.
     */
    @Test
    public void shouldReturnExpectedResultsUsingBefore() {
        assertThat(repo.findByPublicationYearBefore(1980), containsInAnyOrder(
                books.stream().filter(book -> book.getPublicationYear() < 1980).toArray()));
    }

    /**
     * Validate the expected result is returned when querying for {@link Book books} {@code after} a specific year.
     */
    @Test
    public void shouldReturnExpectedResultsUsingAfter() {
        assertThat(repo.findByPublicationYearAfter(1980), containsInAnyOrder(
                books.stream().filter(book -> book.getPublicationYear() > 1980).toArray()));
    }

    /**
     * Validate the expected result is returned when searching by a title containing the given string.
     */
    @Test
    public void shouldFindBooksUsingContains() {
        assertThat(repo.findByTitleContains("Dune"), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().contains("Dune")).toArray()));
    }

    /**
     * Validate the expected result is returned when searching for books with pages numbered greater than a
     * given value.
     */
    @Test
    void shouldFindBooksWithPagesGreaterThan() {
        assertThat(repo.findByPagesGreaterThan(468), containsInAnyOrder(
                books.stream().filter(book -> book.getPages() > 468).toArray()));
    }

    /**
     * Validate the expected result is returned when searching for books with pages numbered less than a
     * given value.
     */
    @Test
    void shouldFindBooksWithPagesLessThan() {
        assertThat(repo.findByPagesLessThan(677), containsInAnyOrder(
                books.stream().filter(book -> book.getPages() < 677).toArray()));
    }

    /**
     * Validate the expected results are returned when searching for titles starting with a given string.
     */
    @Test
    void shouldFindByTitleStartingWith() {
        assertThat(repo.findByTitleStartingWith("Du"), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().startsWith("Du")).toArray()));
    }

    /**
     * Validate the expected results are returned when searching for titles ending with a given string.
     */
    @Test
    void shouldFindByTitleEndingWith() {
        assertThat(repo.findByTitleEndingWith("Wind"), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().endsWith("Wind")).toArray()));
    }

    /**
     * Validate the expected results are returned when searching for a list of titles.
     */
    @Test
    void shouldFindByTitleIn() {
        List<String> titles = new ArrayList<>();
        titles.add("Dune");
        titles.add("The Name of the Wind");

        assertThat(repo.findByTitleIn(titles), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().equals("Dune")
                        || book.getTitle().equals("The Name of the Wind")).toArray()));
    }

    /**
     * Validate the expected results are returned when searching for books published between a given range.
     */
    @Test
    void shouldFindBetweenPublicationYears() {
        assertThat(repo.findByPublicationYearBetween(1960, 2000), containsInAnyOrder(
                books.stream().filter(book -> book.getPublicationYear() > 1960
                        && book.getPublicationYear() < 2000).toArray()));
    }

    /**
     * Validate the expected results when searching for null authors.
     */
    @Test
    void shouldReturnEmptyListForNullAuthors() {
        assertThat(repo.findByAuthorIsNull(), containsInAnyOrder(
                books.stream().filter(book -> book.getAuthor() == null).toArray()));
    }

    /**
     * Validate the expected results when searching for non-null authors.
     */
    @Test
    void shouldReturnListForNonNullAuthors() {
        assertThat(repo.findByAuthorIsNotNull(), containsInAnyOrder(
                books.stream().filter(book -> book.getAuthor() != null).toArray()));
    }

    /**
     * Validate the expected number of titles with pages greater than input value.
     */
    @Test
    void shouldReturnCountOfTitlesWithPagesGreaterThan() {
        assertThat(repo.countTitleByPagesGreaterThan(400),
                is(books.stream().filter(book -> book.getPages() > 400).count()));
    }

    /**
     * Validate the expected number of distinct titles with pages greater than input value.
     */
    @Test
    void shouldReturnCountDistinctOfTitlesWithPagesGreaterThan() {
        assertThat(repo.countDistinctTitleByPagesGreaterThan(400),
                is(books.stream().filter(book -> book.getPages() > 400).distinct().count()));
    }

    /**
     * Validate the expected results are returned when searching for a list of titles
     * with pages greater than input value.
     */
    @Test
    void shouldReturnListOfDistinctTitlesWithPagesGreaterThan() {
        assertThat(repo.findDistinctTitleByPagesGreaterThan(400), containsInAnyOrder(
                books.stream()
                        .filter(book -> book.getPages() > 400)
                        .map(Book::getTitle)
                        .distinct().toArray()));
    }

    /**
     * Validate the expected value is returned when getting max pages by author.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldReturnMaxPagesByAuthor() {
        assertThat(repo.findMaxPagesByAuthor(FRANK_HERBERT),
                is(books.stream()
                        .filter(book -> book.getAuthor().equals(FRANK_HERBERT))
                        .map(Book::getPages)
                        .max(Comparator.comparing(Long::valueOf)).get().longValue()));
    }

    /**
     * Validate the expected value is returned when getting min pages by author.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldReturnMinPagesByAuthor() {
        assertThat(repo.findMinPagesByAuthor(FRANK_HERBERT),
                is(books.stream()
                        .filter(book -> book.getAuthor().equals(FRANK_HERBERT))
                        .map(Book::getPages)
                        .min(Comparator.comparing(Long::valueOf)).get().longValue()));
    }

    /**
     * Validate the expected value is returned when getting sum pages by author.
     */
    @Test
    void shouldReturnSumPagesByAuthor() {
        assertThat(repo.findSumPagesByAuthor(FRANK_HERBERT),
                is(books.stream()
                        .filter(book -> book.getAuthor().equals(FRANK_HERBERT))
                        .map(Book::getPages)
                        .reduce(0, Integer::sum).longValue()));
    }

    /**
     * Validate the expected value is returned when getting avg pages by author.
     */
    @Test
    void shouldReturnAvgPagesByAuthor() {
        assertThat(repo.findAvgPagesByAuthor(FRANK_HERBERT),
                is(books.stream()
                        .filter(book -> book.getAuthor().equals(FRANK_HERBERT))
                        .collect(Collectors.averagingInt(Book::getPages)).longValue()));
    }

    /**
     * Validate batch updates work as expected.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldSupportBatchUpdates() {
        repo.updateByTitleStartingWith("Du", 700);
        assertThat(repo.findById(DUNE.getUuid()).get().getPages(), is(700));
        assertThat(repo.findById(DUNE_MESSIAH.getUuid()).get().getPages(), is(700));
    }

    /**
     * Validate single update with existing value returns the expected value and updates
     * the book.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldSupportSingleUpdates() {
        assertThat(repo.update(DUNE.getUuid(), 999), is(1));
        assertThat(repo.findById(DUNE.getUuid()).get().getPages(), is(999));
    }

    /**
     * Validate expected return value when the no entity matches.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldSupportSingleUpdatesNoMatch() {
        assertThat(repo.update(new UUID(), 999), is(0));
        assertThat(repo.findById(DUNE.getUuid()).get().getPages(), is(DUNE.getPages()));
    }

    /**
     * Validate batch deletes work as expected.
     */
    @Test
    void shouldSupportBatchDeletes() {
        repo.deleteByTitleStartingWith("Du");
        assertThat(repo.count(), is(2L));
        assertThat(repo.findById(DUNE.getUuid()).isPresent(), is(false));
        assertThat(repo.findById(DUNE_MESSIAH.getUuid()).isPresent(), is(false));
    }

    /**
     * Validate bulk saves work as expected.
     */
    @Test
    void shouldSupportBulksSaves() {
        Set<Book> setNewBooks = new HashSet<>();
        setNewBooks.add(new Book("Children of Dune", 444, FRANK_HERBERT, new GregorianCalendar(1976,
                Calendar.APRIL, 6, 0, 0)));
        setNewBooks.add(new Book("God Emperor of Dune", 496, FRANK_HERBERT, new GregorianCalendar(1981,
                Calendar.FEBRUARY, 6, 0, 0)));
        repo.saveBooks(setNewBooks);

        assertThat(repo.findByTitleIn(Arrays.asList("Children of Dune", "God Emperor of Dune")),
                containsInAnyOrder(setNewBooks.toArray()));
    }
}
