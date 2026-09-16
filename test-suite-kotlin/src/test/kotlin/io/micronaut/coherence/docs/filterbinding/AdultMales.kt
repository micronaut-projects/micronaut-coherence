package io.micronaut.coherence.docs.filterbinding

// tag::imports[]
import io.micronaut.coherence.annotation.FilterBinding
// end::imports[]

// tag::clazz[]
@FilterBinding                         // <1>
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AdultMales            // <2>
// end::clazz[]
