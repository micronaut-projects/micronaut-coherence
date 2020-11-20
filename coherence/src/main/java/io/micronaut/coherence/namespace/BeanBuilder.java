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

import java.util.Objects;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.builder.ParameterizedBuilder;
import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.Expression;
import com.tangosol.config.expression.LiteralExpression;
import com.tangosol.config.expression.ParameterResolver;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;

/**
 * Element processor for {@code <bean>} XML element.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@SuppressWarnings("deprecation")
class BeanBuilder implements ParameterizedBuilder<Object>, ParameterizedBuilder.ReflectionSupport {

    /**
     * The {@link io.micronaut.context.ApplicationContext} to use to
     * look-up named beans.
     */
    private final ApplicationContext context;

    /**
     * The {@link com.tangosol.config.expression.Expression} that will
     * resolve to the bean name to look-up.
     */
    private final Expression<String> beanNameExpression;

    /**
     * Construct a {@code BeanBuilder} instance.
     *
     * @param context            the {@link io.micronaut.context.ApplicationContext}
     *                           to use to look-up beans.
     * @param beanNameExpression the expression that will resolve the name of the CDI bean
     */
    BeanBuilder(ApplicationContext context, String beanNameExpression) {
        this.context = Objects.requireNonNull(context);
        this.beanNameExpression = new LiteralExpression<>(beanNameExpression);
    }

    @Override
    public Object realize(ParameterResolver resolver, ClassLoader loader, ParameterList parameterList) {
        String beanName = this.beanNameExpression.evaluate(resolver);
        return context.findBean(Object.class, Qualifiers.byName(beanName))
                .orElseThrow(() -> new ConfigurationException(String.format("Cannot resolve bean '%s', ", beanName),
                                     "Ensure that a bean with that name exists and can be discovered"));
    }

    @Override
    public boolean realizes(Class<?> aClass, ParameterResolver resolver, ClassLoader loader) {
        String beanName = this.beanNameExpression.evaluate(resolver);
        return context.findBean(aClass, Qualifiers.byName(beanName)).isPresent();
    }
}
