/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.coherence.data.interceptors;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.coherence.data.ops.DefaultCoherenceRepositoryOperations;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.Qualifier;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.order.Ordered;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.operations.PrimaryRepositoryOperations;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A copy of Micronaut Data's {@link io.micronaut.data.intercept.DataIntroductionAdvice} that will provide a default
 * {@link RepositoryOperations} instance using the default Coherence Session
 * configuration is present for the target {@code NamedMap}.
 *
 * @since 3.0.1
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Singleton
public class CoherenceDataIntroductionAdvice implements MethodInterceptor<Object, Object> {
    private final BeanLocator beanLocator;
    private final ConversionService conversionService;
    private final Map<RepositoryMethodKey, DataInterceptor> interceptorMap = new ConcurrentHashMap<>(20);

    /**
     * Default constructor.
     * @param beanLocator The bean locator
     */
    CoherenceDataIntroductionAdvice(BeanLocator beanLocator, ConversionService conversionService) {
        this.beanLocator = beanLocator;
        this.conversionService = conversionService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        RepositoryMethodKey key = new RepositoryMethodKey(context.getTarget(), context.getExecutableMethod());
        DataInterceptor<Object, Object> dataInterceptor = interceptorMap.get(key);
        if (dataInterceptor == null) {
            String dataSourceName = context.stringValue(Repository.class).orElse(null);
            Class<?> operationsType = context.classValue(RepositoryConfiguration.class, "operations")
                    .orElse(PrimaryRepositoryOperations.class);
            Class<?> interceptorType = context
                    .classValue(DataMethod.class, DataMethod.META_MEMBER_INTERCEPTOR)
                    .orElse(null);

            if (interceptorType != null && DataInterceptor.class.isAssignableFrom(interceptorType)) {
                DataInterceptor<Object, Object> childInterceptor =
                        findInterceptor(dataSourceName, operationsType, interceptorType);
                interceptorMap.put(key, childInterceptor);
                return childInterceptor.intercept(key, context);
            } else {
                final AnnotationValue<DataMethod> declaredAnnotation = context.getDeclaredAnnotation(DataMethod.class);
                if (declaredAnnotation != null) {
                    interceptorType = declaredAnnotation.classValue(DataMethod.META_MEMBER_INTERCEPTOR).orElse(null);
                    if (interceptorType != null && DataInterceptor.class.isAssignableFrom(interceptorType)) {
                        DataInterceptor<Object, Object> childInterceptor =
                                findInterceptor(dataSourceName, operationsType, interceptorType);
                        interceptorMap.put(key, childInterceptor);
                        return childInterceptor.intercept(key, context);
                    }
                }

                final String interceptorName = context.getAnnotationMetadata()
                        .stringValue(DataMethod.class, DataMethod.META_MEMBER_INTERCEPTOR)
                        .orElse(null);
                if (interceptorName != null) {
                    throw new IllegalStateException("Micronaut Data Interceptor [" + interceptorName + "] is not on" +
                            " the classpath but required by the method: " + context.getExecutableMethod());
                }
                throw new IllegalStateException("Micronaut Data method is missing compilation time query " +
                        "information. Ensure that the Micronaut Data annotation processors are declared in your" +
                        " build and try again with a clean re-build.");
            }
        } else {
            return dataInterceptor.intercept(key, context);
        }

    }

    private @NonNull
    DataInterceptor<Object, Object> findInterceptor(
            @Nullable String dataSourceName,
            @NonNull Class<?> operationsType,
            @NonNull Class<?> interceptorType) {
        DataInterceptor interceptor;
        if (!RepositoryOperations.class.isAssignableFrom(operationsType)) {
            throw new IllegalArgumentException("Repository type must be an instance of RepositoryOperations!");
        }

        Qualifier qualifier = Qualifiers.byName(dataSourceName);
        RepositoryOperations datastore;
        try {
            datastore = (RepositoryOperations) beanLocator.getBean(operationsType, qualifier);
        } catch (NoSuchBeanException e) {
            // if there is no explicit configuration, use a DefaultCoherenceRepositoryOperations implementation
            // using the default Coherence session
            datastore = new DefaultCoherenceRepositoryOperations(dataSourceName,
                                                                 (ApplicationContext) beanLocator,
                                                                 conversionService,
                                                                 (BeanContext) beanLocator);
            ((ApplicationContext) beanLocator).registerSingleton(RepositoryOperations.class, datastore, qualifier);
        }
        BeanIntrospection<Object> introspection = BeanIntrospector.SHARED.findIntrospections(
                ref -> interceptorType.isAssignableFrom(
                        ref.getBeanType())).stream().findFirst().orElseThrow(() ->
                                new DataAccessException("No Data interceptor found for type: " + interceptorType)
        );
        if (introspection.getConstructorArguments().length == 0) {
            interceptor = (DataInterceptor) introspection.instantiate();
        } else {
            interceptor = (DataInterceptor) introspection.instantiate(datastore);
        }
        return interceptor;
    }
}
