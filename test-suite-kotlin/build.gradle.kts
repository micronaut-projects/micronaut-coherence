plugins {
    id("io.micronaut.build.internal.coherence-test-suite")
    id("io.micronaut.build.internal.kotlin-ksp")
}

dependencies {
    kspTest(mn.micronaut.inject.kotlin)
    kspTest(mnData.micronaut.data.processor)
    kspTest(projects.micronautCoherenceData)
    testImplementation(mnTest.micronaut.test.junit5)
}
