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
package io.micronaut.coherence.namespace;

import com.tangosol.config.xml.AbstractNamespaceHandler;

/**
 * Custom namespace handler for the {@code micronaut} namespace.
 * <p>
 * This namespace handler supports only one XML element:
 * <ul>
 * <li>{@code &lt;cdi:bean>beanName&lt;/cdi:bean>}, where {@code beanName}
 * is the unique name of a Micronaut bean defined by the {@code @Named} annotation.
 * This element can only be used as a child of the standard {@code
 * &lt;instance>} element.</li>
 * </ul>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public class MicronautNamespaceHandler extends AbstractNamespaceHandler {
    /**
     * Construct a {@code MicronautNamespaceHandler} instance.
     */
    public MicronautNamespaceHandler() {
        registerProcessor("bean", new BeanProcessor());
    }
}
