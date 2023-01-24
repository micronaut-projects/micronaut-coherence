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
import com.tangosol.net.Session;
import io.micronaut.coherence.data.model.Book;
import io.micronaut.coherence.data.repositories.CoherenceBook2Repository;
import io.micronaut.coherence.data.repositories.CoherenceBook3Repository;
import io.micronaut.coherence.data.repositories.CoherenceBookRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Validation of {@link AbstractCoherenceRepository}.
 */
@MicronautTest(propertySources = {"classpath:sessions.yaml"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepositoryTest extends AbstractDataTest {

    /**
     * Defined in configuration and using the default Session.
     */
    @Inject
    protected CoherenceBookRepository repo;

    /**
     * Defined in configuration and using the {@code custom} Session.
     */
    @Inject
    protected CoherenceBook2Repository repo2;

    /**
     * Not defined in configuration using the default Session.
     */
    @Inject
    protected CoherenceBook3Repository repo3;

    /**
     * Custom {@code Session}.
     */
    @Inject
    protected Session custom;

    // ----- test methods ---------------------------------------------------

    /**
     * Ensure the {@link io.micronaut.coherence.data.interceptors.GetMapInterceptor} interceptor fires when
     * {@link AbstractCoherenceRepository#getMap()} is invoked.
     */
    @Test
    public void shouldReturnNamedMap() {
        assertThat(repo, notNullValue());
        assertThat(repo.getMap(), instanceOf(NamedMap.class));
        assertThat(repo.count(), is(4L));
    }

    /**
     * Ensure the {@link io.micronaut.coherence.data.interceptors.GetIdInterceptor} interceptor fires when
     * {@link AbstractCoherenceRepository#getId(Object)} is invoked.
     */
    @Test
    public void shouldReturnId() {
        assertThat(repo, notNullValue());
        assertThat(repo.getId(DUNE), is(DUNE.getUuid()));
    }

    /**
     * Ensure the {@link io.micronaut.coherence.data.interceptors.GetEntityTypeInterceptor} interceptor fires when
     * {@link AbstractCoherenceRepository#getEntityType()} is invoked.
     */
    @Test
    public void shouldReturnEntityType() {
        assertThat(repo, notNullValue());
        assertThat(repo.getEntityType(), Matchers.typeCompatibleWith(Book.class));
    }

    /**
     * Ensure generated queries continue to work when extending {@code AbstractCoherenceRepository}.
     */
    @Test
    public void shouldAllowGeneratedQueries() {
        assertThat(repo.findByTitleStartingWith("Du"), containsInAnyOrder(
                books.stream().filter(book -> book.getTitle().startsWith("Du")).toArray()));
    }

    /**
     * Ensure custom session is used properly.
     *
     * @since 3.0.1
     */
    @Test
    public void shouldUseCustomSession() {
        assertThat(repo2, notNullValue());
        assertThat(repo2.getEntityType(), Matchers.typeCompatibleWith(Book.class));
        assertThat(custom.getMap("book2").size(), is(4));
    }

    /**
     * Ensure it's possible to create a Repository that has zero configuration.
     *
     * @since 3.0.1
     */
    @Test
    public void shouldCreateRepoWithNoConfig() {
        assertThat(repo3, notNullValue());
        assertThat(repo3.getEntityType(), Matchers.typeCompatibleWith(Book.class));
    }
}
