package io.micronaut.coherence.docs.model;

import com.tangosol.util.UUID;
import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.io.Serializable;

/**
 * A book entity stored by the Micronaut Data repositories of the examples.
 */
@MappedEntity
public class Book implements Serializable {

    @Id
    private final UUID uuid;
    private final String title;
    private final int pages;

    @Creator
    public Book(UUID uuid, String title, int pages) {
        this.uuid = uuid;
        this.title = title;
        this.pages = pages;
    }

    public Book(String title, int pages) {
        this(new UUID(), title, pages);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public int getPages() {
        return pages;
    }
}
