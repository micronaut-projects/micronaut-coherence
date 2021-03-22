package io.micronaut.coherence.data.util;

import java.util.Objects;

public class EventRecord<T> {
    public final EventType eventType;
    public final T entity;

    public EventRecord(final EventType eventType, final T entity) {
        this.eventType = eventType;
        this.entity = entity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EventRecord<?> that = (EventRecord<?>) o;
        return eventType == that.eventType && entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, entity);
    }

    @Override
    public String toString() {
        return "EventRecord{" +
                "eventType=" + eventType +
                ", entity=" + entity +
                '}';
    }
}
