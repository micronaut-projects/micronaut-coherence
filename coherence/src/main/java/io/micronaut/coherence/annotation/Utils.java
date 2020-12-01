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
package io.micronaut.coherence.annotation;

import java.lang.annotation.Annotation;
import java.util.*;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.util.StringUtils;
import io.micronaut.messaging.annotation.SendTo;

/**
 * Annotation utilities.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public final class Utils {

    private static final String[] EMPTY = new String[0];

    /**
     * Utility class.
     */
    private Utils() {
    }

    public static String getFirstTopicName(AnnotationMetadata metadata, String defaultName) {
        String[] names = getTopicNames(metadata);
        return names.length == 0 ? defaultName : names[0];
    }

    public static Optional<String> getFirstTopicName(AnnotationMetadata metadata) {
        String[] names = getTopicNames(metadata);
        return names.length == 0 ? Optional.empty() : Optional.of(names[0]);
    }

    public static String[] getTopicNames(AnnotationMetadata metadata) {
        Optional<AnnotationValue<Topics>> optional = metadata.findAnnotation(Topics.class);
        if (optional.isPresent()) {
            List<AnnotationValue<Annotation>> list = optional.get().getAnnotations("value");
            if (list.isEmpty()) {
                return EMPTY;
            }

            Set<String> names = new HashSet<>();
            for (AnnotationValue<Annotation> annotation : list) {
                Collections.addAll(names, annotation.stringValues());
            }
            return names.stream()
                    .filter(StringUtils::isNotEmpty)
                    .toArray(String[]::new);
        } else {
            return metadata.stringValue(Topic.class)
                           .map(name -> new String[]{name})
                                   .orElse(metadata.stringValue(Topic.class)
                                   .map(name -> new String[]{name})
                           .orElse(EMPTY));
        }
    }

    public static String[] getSendToTopicNames(AnnotationMetadata metadata) {
        return Arrays.stream(metadata.stringValues(SendTo.class))
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);
    }

    public static Optional<String> getSendToTopicName(AnnotationMetadata metadata) {
        return Arrays.stream(metadata.stringValues(SendTo.class))
                .filter(StringUtils::isNotEmpty)
                .findFirst();
    }
}
