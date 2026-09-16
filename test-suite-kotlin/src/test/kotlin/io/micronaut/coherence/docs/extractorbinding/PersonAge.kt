package io.micronaut.coherence.docs.extractorbinding

// tag::imports[]
import io.micronaut.coherence.annotation.ExtractorBinding
// end::imports[]

// tag::clazz[]
@ExtractorBinding                         // <1>
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonAge                // <2>
// end::clazz[]
