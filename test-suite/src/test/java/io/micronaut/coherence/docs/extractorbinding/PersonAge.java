package io.micronaut.coherence.docs.extractorbinding;

// tag::imports[]
import io.micronaut.coherence.annotation.ExtractorBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
// end::imports[]

// tag::clazz[]
@ExtractorBinding                         // <1>
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PersonAge {            // <2>
}
// end::clazz[]
