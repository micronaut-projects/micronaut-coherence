/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.coherence.events;

import javax.inject.Singleton;

import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.annotation.EntryEvents;
import com.tangosol.net.events.annotation.Interceptor;
import com.tangosol.net.events.partition.cache.EntryEvent;

@Singleton
@Interceptor
@EntryEvents({EntryEvent.Type.INSERTED, EntryEvent.Type.UPDATED, EntryEvent.Type.REMOVED})
public class MyInterceptor implements EventInterceptor<EntryEvent<?, ?>> {
    @Override
    public void onEvent(EntryEvent<?, ?> event) {
        // process the event.
    }
}
