package io.micronaut.coherence.docs.injection.maps

import com.tangosol.net.cache.ContinuousQueryCache
import io.micronaut.coherence.docs.model.Person
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "NamedMapInjectionTest")
@MicronautTest
class NamedMapInjectionTest {

    @Inject
    lateinit var peopleService: PeopleService

    @Inject
    lateinit var someBean: SomeBean

    @Inject
    lateinit var catalogService: CatalogService

    @Inject
    lateinit var catalogController: CatalogController

    @Inject
    lateinit var asyncPeopleService: AsyncPeopleService

    @Inject
    lateinit var peopleViews: PeopleViews

    @Test
    fun testInjectNamedMap() {
        assertEquals("people", peopleService.people.name)
        assertEquals("people", peopleService.map.name)
        assertEquals("people", someBean.map.name)
        assertEquals("people", asyncPeopleService.map.namedMap.name)

        peopleService.people["homer"] = Person("Homer", "Simpson", 39, "male")
        assertEquals("Homer", peopleService.map["homer"]!!.firstName)
        assertEquals("Homer", someBean.map["homer"]!!.firstName)
        assertEquals("Homer", asyncPeopleService.map["homer"].join().firstName)
    }

    @Test
    fun testInjectNamedMapFromSession() {
        val products = catalogService.map
        assertEquals("products", products.name)
        assertEquals("products", catalogController.products.name)
        assertEquals("Catalog", products.service.backingMapManager.cacheFactory.scopeName)
        assertNotEquals(peopleService.people.service, products.service)
    }

    @Test
    fun testInjectViews() {
        val people = peopleService.people
        people["homer"] = Person("Homer", "Simpson", 39, "male")
        people["marge"] = Person("Marge", "Simpson", 36, "female")
        people["maggie"] = Person("Maggie", "Simpson", 1, "female")
        people["ned"] = Person("Ned", "Flanders", 60, "male")

        assertInstanceOf(ContinuousQueryCache::class.java, peopleViews.map)
        assertEquals(4, peopleViews.map.size)

        assertEquals(3, peopleViews.simpsons.size)
        assertTrue(peopleViews.simpsons.values.all { it.lastName == "Simpson" })

        assertEquals(setOf("homer"), peopleViews.adultMaleSimpsons.keys)

        assertEquals(4, peopleViews.ages.size)
        assertEquals(listOf(1, 36, 39, 60), peopleViews.ages.values.sorted())
    }
}
