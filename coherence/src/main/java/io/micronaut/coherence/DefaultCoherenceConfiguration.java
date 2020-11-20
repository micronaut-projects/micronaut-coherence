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
package io.micronaut.coherence;

import javax.inject.Named;

import com.tangosol.net.Coherence;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;

/**
 * The default Coherence configuration bean.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Named(value = Coherence.DEFAULT_NAME)
@Primary
@Requires(property = DefaultCoherenceConfiguration.PREFIX + "." + "enabled", value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
@ConfigurationProperties(DefaultCoherenceConfiguration.PREFIX)
class DefaultCoherenceConfiguration implements Toggleable {
    public static final String PREFIX = "coherence";

    private boolean enabled;

    /**
     * Sets whether the DefaultCoherenceConfiguration is enabled.
     *
     * @param enabled True if it is.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
