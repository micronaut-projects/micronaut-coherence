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

import com.oracle.coherence.inject.Injector;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.InitializingBeanDefinition;

/**
 * An implementation of an {@link com.oracle.coherence.inject.Injector}
 * that will inject beans into a class using the Micronaut bean context.
 * <p>An instance of this class will be created by Coherence using the
 * {@link java.util.ServiceLoader}.</p>
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public class InjectorImpl implements Injector {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void inject(Object target) {
        ApplicationContext ctx = CoherenceContext.getApplicationContext();
        BeanDefinition bd = ctx.getBeanDefinition(target.getClass());
        if (bd instanceof InitializingBeanDefinition initializingBeanDefinition) {
            initializingBeanDefinition.initialize(ctx, target);
        }
        ctx.inject(target);
    }
}
