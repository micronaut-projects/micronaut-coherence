/*
 * Copyright 2017-2022 original authors
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

import io.micronaut.coherence.data.interceptors.AbstractEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.AbstractEventSourceInterceptor.EventGroup;
import io.micronaut.coherence.data.interceptors.AbstractEventSourceInterceptor.EventType;
import io.micronaut.coherence.data.interceptors.AsyncPersistEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.AsyncRemoveEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.AsyncUpdateEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.PersistEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.RemoveEventSourceInterceptor;
import io.micronaut.coherence.data.interceptors.UpdateEventSourceInterceptor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for various interceptors used by the micronaut-data implementation.
 */
class InterceptorTest {
    @Test
    void ensureExpectedEventGroupsForPersistInterceptor() {
        validatePersistInterceptor(new PersistEventSourceInterceptor(null));
    }

    @Test
    void ensureExpectedEventGroupsForAsyncPersistInterceptor() {
        validatePersistInterceptor(new AsyncPersistEventSourceInterceptor(null));
    }

    @Test
    void ensureExpectedEventGroupsForUpdateInterceptor() {
        validateUpdateInterceptor(new UpdateEventSourceInterceptor(null));
    }

    @Test
    void ensureExpectedEventGroupsForAsyncUpdateInterceptor() {
        validateUpdateInterceptor(new AsyncUpdateEventSourceInterceptor(null));
    }

    @Test
    void ensureExpectedEventGroupsForRemoveInterceptor() {
        validateRemoveInterceptor(new RemoveEventSourceInterceptor(null));
    }

    @Test
    void ensureExpectedEventGroupsForAsyncRemoveInterceptor() {
        validateRemoveInterceptor(new AsyncRemoveEventSourceInterceptor(null));
    }

    private void validatePersistInterceptor(AbstractEventSourceInterceptor interceptor) {
        validateInterceptor(interceptor, EventGroup.PERSIST, EventType.PRE_PERSIST, EventType.POST_PERSIST);
    }

    private void validateUpdateInterceptor(AbstractEventSourceInterceptor interceptor) {
        validateInterceptor(interceptor, EventGroup.UPDATE, EventType.PRE_UPDATE, EventType.POST_UPDATE);
    }

    private void validateRemoveInterceptor(AbstractEventSourceInterceptor interceptor) {
        validateInterceptor(interceptor, EventGroup.REMOVE, EventType.PRE_REMOVE, EventType.POST_REMOVE);
    }

    private void validateInterceptor(AbstractEventSourceInterceptor interceptor, EventGroup group,
                                            EventType preOp, EventType postOp) {
        assertThat(interceptor.getEventGroup(), is(group));
        assertThat(interceptor.getHandledPreEventType(), is(preOp));
        assertThat(interceptor.getHandledPostEventType(), is(postOp));
    }
}
