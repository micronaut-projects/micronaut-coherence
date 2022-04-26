package io.micronaut.coherence.data;

import com.tangosol.util.UUID;
import io.micronaut.coherence.data.model.Book;
import io.micronaut.coherence.data.repositories.AsyncBookRepository;
import io.micronaut.coherence.data.repositories.BookRepository;
import io.micronaut.coherence.data.repositories.CoherenceAsyncBookRepository;
import io.micronaut.coherence.data.repositories.CoherenceBookRepository;
import io.micronaut.coherence.data.util.EventRecord;
import io.micronaut.coherence.data.util.EventType;
import io.micronaut.context.BeanContext;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.async.AsyncCrudRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Validate persist event eviction logic.
 */
@MicronautTest(propertySources = {"classpath:sessions.yaml"}, environments = "evict-persist")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersistEventEvictionsTest extends AbstractDataTest {
    /**
     * A sync repo that extends {@link AbstractCoherenceRepository}.
     */
    @Inject
    protected CoherenceBookRepository repo;

    /**
     * A {@code repository} implementing {@link CrudRepository}.
     */
    @Inject
    protected BookRepository crudRepo;

    /**
     * A sync repo that extends {@link AbstractCoherenceAsyncRepository}.
     */
    @Inject
    protected CoherenceAsyncBookRepository repoAsync;

    /**
     * A {@code repository} implementing {@link AsyncCrudRepository}.
     */
    @Inject
    protected AsyncBookRepository crudRepoAsync;

    /**
     * Micronaut {@link BeanContext}.
     */
    @Inject
    protected BeanContext beanContext;

    // ----- test methods ---------------------------------------------------

    /**
     * Validate event listener returning false results in the entity not being persisted using {@link #crudRepo}.
     */
    @Test
    public void shouldValidatePrePersistEvictionSyncRepo() {
        runPersistEventTestEviction(crudRepo);
    }

    /**
     * Validate event listener returning false results in the entity not being persisted using {@link #crudRepoAsync}.
     */
    @Test
    public void shouldValidatePrePersistEvictionAsyncRepo() {
        runPersistEventTestEviction(crudRepoAsync);
    }

    // ----- helper methods -------------------------------------------------

    /**
     * Validate eviction behavior.
     *
     * @param repository the {@link CrudRepository} under test
     */
    private void runPersistEventTestEviction(CrudRepository<Book, UUID> repository) {
        assertThat(repository.existsById(IT.getUuid()), is(false));
        Book result = repository.save(IT);
        assertThat(result, is(IT));
        assertThat(repository.existsById(IT.getUuid()), is(false));
        assertThat(eventRecorder.getRecordedEvents(), contains(
                new EventRecord<>(EventType.PRE_PERSIST, IT)));
    }

    /**
     * Validate eviction behavior.
     *
     * @param repository the {@link AsyncCrudRepository} under test
     */
    private void runPersistEventTestEviction(AsyncCrudRepository<Book, UUID> repository) {
        repository.existsById(IT.getUuid())
                .thenAccept(exists -> assertThat(exists, is(false)))
                .thenCompose(unused -> repository.save(IT))
                .thenAccept(book1 -> assertThat(book1, is(IT)))
                .thenCompose(unused -> repository.existsById(IT.getUuid()))
                .thenAccept(exists -> assertThat(exists, is(false)))
                .thenAccept(unused -> assertThat(eventRecorder.getRecordedEvents(), contains(
                        new EventRecord<>(EventType.PRE_PERSIST, IT)))).join();
    }
}
