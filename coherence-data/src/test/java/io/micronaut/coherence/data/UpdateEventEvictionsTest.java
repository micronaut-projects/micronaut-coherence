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
 * Validate update event eviction logic.
 */
@MicronautTest(propertySources = {"classpath:sessions.yaml"}, environments = "evict-update")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UpdateEventEvictionsTest extends AbstractDataTest {
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
     * Validate event listener returning false results in the entity not being updated using {@link #crudRepo}.
     */
    @Test
    public void shouldValidatePreUpdateEvictionSyncRepo() {
        runUpdateEventTestEviction(crudRepo);
    }

    /**
     * Validate event listener returning false results in the entity not being updated using {@link #repo}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldValidatePreUpdateEvictionSyncRepoCoherence() {
        runUpdateEventTestEviction(asCrudRepo(repo));
    }

    // ----- helper methods -------------------------------------------------

    /**
     * Validate eviction behavior.
     *
     * @param repository the {@link CrudRepository} under test
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void runUpdateEventTestEviction(CrudRepository<Book, UUID> repository) {
        Book duneCopy = new Book(DUNE);
        duneCopy.setPages(1000);

        Book result = repository.update(duneCopy);
        assertThat(result, is(duneCopy));
        assertThat(repository.findById(DUNE.getUuid()).get(), is(DUNE));
        assertThat(eventRecorder.getRecordedEvents(), contains(
                new EventRecord<>(EventType.PRE_UPDATE, duneCopy)));
    }
}
