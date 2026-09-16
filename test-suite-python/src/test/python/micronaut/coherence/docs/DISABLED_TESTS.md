# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut Coherence that are present but disabled, or that deviate from
the Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task
list for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 6 (`NamedMapInjectionTest` x3, `FilterBindingTest`, `ExtractorBindingTest`, `XmlInjectionTest`).
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1` (Coherence runs embedded in the
  test JVM, no container is needed).
- Last full-suite result: build successful, 18 tests, 6 skipped (see below), 0 failures.

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
- Tests are `@MicronautTest` classes with `@Property(name="spec.name", ...)` and injected beans; Java classes are
  imported normally (`from com.tangosol.net import Session`, `from reactor.core.publisher import Mono`, the Java test
  helpers of this project as `from micronaut.coherence.examples import EventsHelper`). Python classes are imported
  as Python modules (`from .ProductClient import ProductClient`) and are usable as runtime type arguments
  (`ApplicationContext.getBean(ProductClient)`); no `java.type(...)` alias is used.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `micronaut.coherence.docs.injection.maps.NamedMapInjectionTest` (3 tests) | A Java `Map` injected into a Python bean (attribute or constructor parameter) is coerced to a copy by `PythonCoercion.coerceToContext` (every `Map`/`List`/`Set` value is rebuilt as a plain collection), so an injected `NamedMap`/`NamedCache`/`ContinuousQueryCache` is a detached snapshot without the `NamedMap` API (`foreign object has no attribute 'getName'`, no live data). `AsyncNamedMap`, `NamedTopic`, `Publisher`, `Subscriber` and `Session` are injected correctly. The user guide carries a `[.lang-python]` note recommending `Session.getMap(...)`. |
| `micronaut.coherence.docs.filterbinding.FilterBindingTest` | Same as above: the injected `@View` maps are copies. The filter factory and the custom binding annotation themselves work (they are resolved when the view is created). |
| `micronaut.coherence.docs.extractorbinding.ExtractorBindingTest` | Same as above. |
| `micronaut.coherence.docs.xmlinjection.XmlInjectionTest` | The `<m:bean>Foo</m:bean>` injection of the Python `MyInterceptor` bean into the cache configuration works and the interceptor is invoked, but the Coherence `@Interceptor` / `@EntryEvents([INSERTED, UPDATED, REMOVED])` annotations (non-Micronaut annotations) are not copied onto the generated Java class, so Coherence registers the interceptor for all entry event types: `['INSERTING:a', 'INSERTED:a', 'UPDATING:a', 'UPDATED:a', 'REMOVING:a', ...]` instead of the three post-events. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.coherence.examples.model.*` (Java, `src/test/java`) | Coherence serializes the values stored in its caches and published to its topics. The Java classes generated for Python classes hold a reference to the GraalPy object and are not `java.io.Serializable` (nor POF serializable), so a Python `Person`/`Order`/`Product`/`Book` cannot be stored in a Coherence cache or topic: the model classes are Java. The package is deliberately not a sub-package of the `micronaut.coherence.docs` Python package, otherwise the Python import `micronaut.coherence.docs.model` is resolved against the Python package (`ModuleNotFoundError`) instead of the Java one. |
| `messaging/*CommitListener.py`, `messaging/ProductElementListener.py` | `@CoherenceTopicListener` is applied to the class instead of the `@Topic` method as in the Java examples: a decorated Python method is treated as a `@Bean` factory method ("Factory methods declared with @Bean must specify a return type"). Documented with a `[.lang-python]` note. |
| `io.micronaut.coherence.docs.PythonRuntimeInitializer` (Java, `src/test/java`) | The Coherence event listener and topic listener processors are `ExecutableMethodProcessor`s created by `DefaultBeanContext.processExecutableMethodsProcessAtStartup()` before the `@Context` beans (among them the GraalPy runtime) are initialized; a no-op Java `TypeConverter<Object, Object>` injecting the `@Named("python") Context` forces the runtime first. The same class also resolves a `micronaut_runtime` helper eagerly (`PythonContextRuntime.helper(context, "__micronaut_import_module")`): the Coherence lifecycle events are dispatched on several `ForkJoinPool` threads at once, each instantiating a Python listener bean, and the concurrent first load of the `micronaut_runtime` module leaves it partially initialized (`The micronaut_runtime module does not define [__micronaut_import_module]` in `CoherenceEventsTest`). |

## Not Ported (documented with `languages="java,kotlin,groovy"` and a `[.lang-python]` note)

| Snippet | Reason |
| --- | --- |
| `io.micronaut.coherence.docs.repository.BookRepository` (commented out in `repository/BookRepository.py`) | The Java class generated for a Python interface extending `CrudRepository[Book, UUID]` with the *Java* entity class `Book` as type argument declares the inherited `deleteAll(Iterable<? extends E>)` method as `deleteAll(Iterable<? extends ? extends Book> entities)`: "illegal start of type" (`build/classes/python/test/.../BookRepository.java:98`). The same declaration with a Python entity class compiles (micronaut-data examples), but a Python entity cannot be stored in Coherence (see the model classes above). |
| `io.micronaut.coherence.docs.repository.CoherenceBookRepository`, `CoherenceAsyncBookRepository` | A Python class cannot extend a Java class (`AbstractCoherenceRepository`, `AbstractCoherenceAsyncRepository`). The `CrudRepository` interface variant (`BookRepository`) is ported. |
| `io.micronaut.coherence.docs.transientinjection.InjectableBean` | Transient objects deserialized by Coherence must be serializable Java classes (see the model classes above); the injected `ToUpperConverter` service is ported. |

## Intentionally Unsupported Snippet Targets

None.
