# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut Coherence that are present but disabled, or that deviate from
the Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task
list for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: see the "Active `@Disabled` Tests" table below.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci` (Coherence runs embedded in the test JVM, no
  container is needed).

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut Coherence annotations are generated from imports (`from micronaut.coherence.annotation import Name,
  SessionName, View, WhereFilter, CoherenceEventListener, ...`).
- Coherence resources are injected with generic type hints: `people: Annotated[NamedMap[str, Person], Inject]`,
  `orders: Annotated[Subscriber[Order], Inject, Name("orders"), WhereFilter("productId = 'AB1234'")]`; constructor
  injection uses the same `Annotated[...]` hints on the `__init__` parameters.
- Event listeners are `@Singleton` (or `@Controller`) classes with `@CoherenceEventListener` methods; the qualifiers
  of the event parameter (`@MapName`, `@Inserted`, `@ServiceName`, ...) are `Annotated[...]` metadata of the parameter.
- Custom filter / extractor binding annotations are functions returning a decorator, meta-annotated with
  `@FilterBinding` / `@ExtractorBinding`; the factories implement `FilterFactory["AdultMales", Person]` (quoted
  forward reference to the annotation function).
- `@CoherencePublisher` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies. Method
  overloading does not exist in Python: the overload of `ProductClient.sendProduct` with a dynamic topic is ported
  as `send_product_to`. Reactive parameters and return types are Reactive Streams `Publisher[...]` type hints
  (`Mono`/`Flux` are wrapped with `Mono.from_(...)` / `Flux.from_(...)`).
- Model classes stored in Coherence (`Person`, `Order`, `Product`, `Book`) are Java classes of this project
  (`src/test/java/io/micronaut/coherence/examples/model`), see below.
- Tests are `@MicronautTest` classes with `@Property(name="spec.name", ...)` and injected beans; Java types used at
  runtime are looked up with `java.type(...)`.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| (none yet - see the "Not Ported" table) | |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.coherence.examples.model.*` (Java, `src/test/java`) | Coherence serializes the values stored in its caches and published to its topics. The Java classes generated for Python classes hold a reference to the GraalPy object and are not `java.io.Serializable` (nor POF serializable), so a Python `Person`/`Order`/`Product`/`Book` cannot be stored in a Coherence cache or topic: the model classes are Java. The package is deliberately not a sub-package of the `micronaut.coherence.docs` Python package, otherwise the Python import `micronaut.coherence.docs.model` is resolved against the Python package (`ModuleNotFoundError`) instead of the Java one. |
| `io.micronaut.coherence.docs.PythonRuntimeInitializer` (Java, `src/test/java`) | The Coherence event listener and topic listener processors are `ExecutableMethodProcessor`s created by `DefaultBeanContext.processExecutableMethodsProcessAtStartup()` before the `@Context` beans (among them the GraalPy runtime) are initialized; a no-op Java `TypeConverter<Object, Object>` injecting the `@Named("python") Context` forces the runtime first. |

## Not Ported (documented with `languages="java,kotlin,groovy"` and a `[.lang-python]` note)

| Snippet | Reason |
| --- | --- |
| `io.micronaut.coherence.docs.repository.CoherenceBookRepository`, `CoherenceAsyncBookRepository` | A Python class cannot extend a Java class (`AbstractCoherenceRepository`, `AbstractCoherenceAsyncRepository`). The `CrudRepository` interface variant (`BookRepository`) is ported. |
| `io.micronaut.coherence.docs.transientinjection.InjectableBean` | Transient objects deserialized by Coherence must be serializable Java classes (see the model classes above); the injected `ToUpperConverter` service is ported. |

## Intentionally Unsupported Snippet Targets

None.
