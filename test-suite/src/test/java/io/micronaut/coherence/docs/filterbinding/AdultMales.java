package io.micronaut.coherence.docs.filterbinding;

// tag::imports[]
import io.micronaut.coherence.annotation.FilterBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
// end::imports[]

// tag::clazz[]
@FilterBinding                         // <1>
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AdultMales {         // <2>
}
// end::clazz[]
