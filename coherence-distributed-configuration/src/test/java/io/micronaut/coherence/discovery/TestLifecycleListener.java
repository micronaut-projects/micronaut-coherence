/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.coherence.discovery;

import com.tangosol.net.Coherence;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An application bean that records the names of the Coherence instances it saw start.
 */
@Singleton
@Requires(property = "spec.name", value = "CoherenceConfigurationClientBootstrapTest")
class TestLifecycleListener implements Coherence.LifecycleListener {

    static final Set<String> STARTED = ConcurrentHashMap.newKeySet();

    @Override
    public void onEvent(CoherenceLifecycleEvent event) {
        if (event.getType() == CoherenceLifecycleEvent.Type.STARTED) {
            STARTED.add(event.getCoherence().getName());
        }
    }
}
