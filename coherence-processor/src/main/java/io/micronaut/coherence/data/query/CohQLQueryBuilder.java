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
package io.micronaut.coherence.data.query;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.builder.jpa.JpaQueryBuilder;

import java.util.Map;

/**
 * A {@link JpaQueryBuilder} specialized for use with {@code CohQL}.
 */
public class CohQLQueryBuilder extends JpaQueryBuilder {

    /**
     * Default constructor.
     */
    public CohQLQueryBuilder() {
    }

    @Override
    protected String[] buildJoin(String alias, JoinPath joinPath, String joinType, StringBuilder target, Map<String, String> appliedJoinPaths, QueryState queryState) {
        throw new UnsupportedOperationException("Coherence Query Language does not support joins");
    }

    @Override
    public String resolveJoinType(Join.Type jt) {
        throw new UnsupportedOperationException("Coherence Query Language does not support joins");
    }

    @NonNull
    @Override
    protected StringBuilder appendDeleteClause(final StringBuilder queryString) {
        return queryString.append(DELETE_CLAUSE).append(FROM_CLAUSE);
    }
}
