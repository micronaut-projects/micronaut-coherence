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

import java.util.function.Supplier;

import com.tangosol.config.ConfigurationException;
import com.tangosol.config.xml.ElementProcessor;
import com.tangosol.config.xml.ProcessingContext;
import com.tangosol.run.xml.XmlElement;

import io.micronaut.coherence.CoherenceContext;
import io.micronaut.context.ApplicationContext;

/**
 * Element processor for {@code <cdi:bean/>} XML element.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
class BeanProcessor implements ElementProcessor<BeanBuilder> {

    /**
     * The {@link java.util.function.Supplier} used to provide
     * an instance of {@link io.micronaut.context.ApplicationContext}.
     */
    private final Supplier<ApplicationContext> contextSupplier;

    /**
     * The default constructor used by the Coherence XML processor.
     */
    BeanProcessor() {
        this(null);
    }

    /**
     * Create a {@link io.micronaut.coherence.namespace.BeanProcessor}.
     *
     * @param contextSupplier The {@link java.util.function.Supplier} used to provide an
     *                        instance of {@link io.micronaut.context.ApplicationContext}
     */
    BeanProcessor(Supplier<ApplicationContext> contextSupplier) {
        this.contextSupplier = contextSupplier == null ? CoherenceContext::getApplicationContext : contextSupplier;
    }

    @Override
    public BeanBuilder process(ProcessingContext context, XmlElement element) throws ConfigurationException {
        return context.inject(new BeanBuilder(contextSupplier.get(), element.getString()), element);
    }
}
