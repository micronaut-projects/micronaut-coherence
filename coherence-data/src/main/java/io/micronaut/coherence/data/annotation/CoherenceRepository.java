/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.coherence.data.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.coherence.data.ops.CoherenceRepositoryOperations;
import io.micronaut.coherence.data.query.CohQLQueryBuilder;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.RepositoryConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking a {@code Repository} for use with Coherence.
 */
@RepositoryConfiguration(
        queryBuilder = CohQLQueryBuilder.class,
        operations = CoherenceRepositoryOperations.class
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Repository
@Introduction
public @interface CoherenceRepository {

    /**
     * The {@code CoherenceRepository}'s backing {@link com.tangosol.net.NamedCache cache} name.
     *
     * @return the {@code CoherenceRepository}'s backing
     *         {@link com.tangosol.net.NamedCache cache} name
     */
    @AliasFor(annotation = Repository.class, member = "value")
    String value();
}
