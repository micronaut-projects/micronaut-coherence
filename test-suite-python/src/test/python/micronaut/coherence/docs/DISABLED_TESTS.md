# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut Coherence that are present but disabled, or that deviate from
the Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task
list for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1` (Coherence runs embedded in the
  test JVM, no container is needed).
- Last full-suite result (micronaut-core 5.2.3, micronaut-build 8.1.2): build successful, 19 tests, 0 skipped, 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut Coherence annotations are generated from imports (`from micronaut.coherence.annotation import Name,
  SessionName, View, WhereFilter, CoherenceEventListener, ...`).
- Coherence resources are injected with generic type hints: `people: Annotated[NamedMap[str, Person], Inject]`,
  `orders: Annotated[Subscriber[Order], Inject, Name("orders"), WhereFilter("productId = 'AB1234'")]`; constructor
  injection uses the same `Annotated[...]` hints on the `__init__` parameters.
- Event listeners are `@Singleton` (or `@Controller`) classes with `@CoherenceEventListener` methods; the qualifiers
  of the event parameter (`@MapName`, `@Inserted`, `@ServiceName`, ...) are `Annotated[...]` metadata of the parameter.
  `@CoherenceTopicListener` goes on the class or on the `@Topic` method, like in Java.
- Injected `NamedMap`/`NamedCache`/view objects are the Java objects (foreign dicts with the full Coherence API);
  the transformed values of a `@View` with an extractor binding are read through `entrySet()`/`values()` (see below).
- Coherence annotations read reflectively from the Java class (`@Interceptor`, `@EntryEvents`) need the
  `@AllowsReflection` hint (`micronaut.core.annotation`) on the Python class, which copies them onto the generated class.
- Custom filter / extractor binding annotations are functions returning a decorator, meta-annotated with
  `@FilterBinding` / `@ExtractorBinding`; the factories implement `FilterFactory["AdultMales", Person]` (quoted
  forward reference to the annotation function).
- `@CoherencePublisher` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies. Method
  overloading does not exist in Python: the overload of `ProductClient.sendProduct` with a dynamic topic is ported
  as `send_product_to`. Reactive parameters and return types are Reactive Streams `Publisher[...]` type hints
  (`Mono`/`Flux` are wrapped with `Mono.from_(...)` / `Flux.from_(...)`).
- Model classes stored in Coherence (`Person`, `Order`, `Product`, `Book`) are Java classes of this project
  (`src/test/java/io/micronaut/coherence/examples/model`), see below; a Python class stored by Coherence
  (`InjectableBean`) is `@Introspected`, which makes the generated class `Serializable`.
- Tests are `@MicronautTest` classes with `@Property(name="spec.name", ...)` and injected beans; Java classes are
  imported normally (`from com.tangosol.net import Session`, `from reactor.core.publisher import Mono`, the Java test
  helpers of this project as `from micronaut.coherence.examples import EventsHelper`). Python classes are imported
  as Python modules (`from .ProductClient import ProductClient`) and are usable as runtime type arguments
  (`ApplicationContext.getBean(ProductClient)`); no `java.type(...)` alias is used.

## Active `@Disabled` Tests

None.

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.coherence.examples.model.*` (Java, `src/test/java`) | The model classes stored in the caches and published to the topics are shared test infrastructure (not snippet targets) and stay Java. The package is deliberately not a sub-package of the `micronaut.coherence.docs` Python package, otherwise the Python import `micronaut.coherence.docs.model` is resolved against the Python package (`ModuleNotFoundError`) instead of the Java one. |
| `io.micronaut.coherence.docs.PythonPackageInitializer` (Java, `src/test/java`) | The `@CoherenceTopicListener` beans are instantiated on the Coherence thread (`CoherenceTopicListenerProcessor.createSubscribers` on the Coherence started event) at the same time as the JUnit thread instantiates `MessagingTest`, a class of the same Python package. The first import of two modules of a package that is not imported yet, on two threads, deadlocks in the Python import system: `_DeadlockError: deadlock detected by _ModuleLock('micronaut.coherence.docs.messaging.MessagingTest')` (or `...AsyncCommitListener`, depending on which thread detects it) - each thread holds the lock of its module and waits for the package, whose generated initialiser imports the module the other thread holds. A no-op Java `TypeConverter` bean, created before the startup processors, imports the package on the startup thread first. |
| `extractorbinding/ExtractorBindingTest` reads the view through `entrySet()` | The Python mapping access to the injected `ContinuousQueryCache` created for `@View @PersonAge` (`ages["homer"]`, `ages.get("homer")`, `dict(ages)`) returns the untransformed `Person` although `ages.isCacheValues()` is true and the Java `entrySet()` / `values()` return the extracted ages (`[['bart', 10], ['homer', 39]]`). The other views (`NamedMapInjectionTest`, `FilterBindingTest`) are asserted through `size()`, `keySet()` and `values()`. |

## Not Ported (documented with `languages="java,kotlin,groovy"` and a `[.lang-python]` note)

| Snippet | Reason |
| --- | --- |
| `io.micronaut.coherence.docs.repository.CoherenceBookRepository`, `CoherenceAsyncBookRepository` | A Python class extending the abstract `AbstractCoherenceRepository[Book, UUID]` (`AbstractCoherenceAsyncRepository`) compiles, but the generated class bridges the abstract `getMapInternal()` of the Java base to Python instead of leaving it to the implementation Micronaut Data generates: `IllegalArgumentException: No Python member [getMapInternal] found` at `AbstractCoherenceRepository.getMap`. The `CrudRepository` interface variant (`BookRepository`) works and is ported. |

## Intentionally Unsupported Snippet Targets

None.
