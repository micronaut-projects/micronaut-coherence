package io.micronaut.coherence.docs.filterbinding

// tag::imports[]
import io.micronaut.coherence.annotation.FilterBinding
// end::imports[]

// tag::clazz[]
@FilterBinding
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Adults(val value: String)
// end::clazz[]
