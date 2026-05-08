# ClassPool DI Container

`ClassPool` is the lightweight dependency container used by the framework and examples. It handles package scanning, component registration, instance factories, dependency ordering, field/constructor injection, qualifiers, and lifecycle hooks.

## Main Classes

| Class | Role |
|---|---|
| `ClassPool` | Global DI registry and bootstrapper. |
| `DefaultConfiguration` | Registers default framework objects such as Gson, JSON/XML `ObjectMapper`, HTTP/socket helpers, and executors. |
| `BuildGson` | Builds framework Gson instance. |
| `BuildObjectMapper` | Builds JSON/XML Jackson mappers. |
| `LogbackConfig` | Applies default logging configuration. |
| `PackageScanner` | Runtime package scanning helper. |
| `DIScanner` | Compile-time processor that emits `META-INF/class-index-{UUID}.json` for `@Component` classes. |

## Core Annotations

| Annotation | Target | Meaning |
|---|---|---|
| `@Component` | type | Register a class as a DI component. |
| `@Verticle` | type | Register/deploy Vert.x verticle classes as managed components. |
| `@Instance` | method | Register a factory method result as a bean. |
| `@Qualifier` | field/parameter/method/type usage | Disambiguate beans by name/key. |
| `@DependsOn` | type/method | Force dependency ordering before component/factory initialization. |
| `@PostConstruct` | method | Invoke after dependency injection. |

## Bootstrap Shape

Typical flow:

1. Load app config into `CommonConstant.applicationConfig`.
2. Register framework defaults from `DefaultConfiguration`.
3. Scan configured packages and generated class-index resources.
4. Register `@Component` and `@Verticle` classes.
5. Register `@Instance` factory methods.
6. Resolve `@DependsOn` ordering.
7. Instantiate beans.
8. Resolve constructor and field dependencies by type or qualifier.
9. Run `@PostConstruct` hooks.

`ClassPool` uses concurrent maps for global registries. Treat it as process-level state.

## Component Registration

Use `@Component` for normal service/config/helper classes. The processor-side `DIScanner` also scans components at compile time and writes class indexes, letting runtime bootstrap avoid pure reflection-only discovery where generated indexes are available.

Use `@Verticle` for Vert.x verticles that the framework should treat as managed components.

Avoid renaming packages casually. Component scanning and generated route code depend on package names.

## Factory Instances

`@Instance` marks a factory method. The returned object is registered in the pool.

Common pattern:

```java
@Component
public class AppConfig {
    @Instance
    public SomeClient someClient() {
        return new SomeClient();
    }
}
```

`@DependsOn` can be applied to factory methods when the instance requires another bean/config to be ready first.

## Dependency Resolution

The container resolves dependencies by:

- concrete type
- assignable interface/supertype
- `@Qualifier` when multiple candidates exist
- factory method return type for `@Instance`

Constructor and field injection are both used in source. Match existing style in the package you edit.

If duplicate instances are possible, use `@Qualifier` rather than relying on registration order.

## Lifecycle

`@PostConstruct` methods run after dependency injection. Use them for local initialization that needs injected dependencies already present.

Keep `@PostConstruct` side effects small. Expensive I/O or route/database bootstrapping should usually live in Vert.x startup code or explicit service methods.

## Ordering

`@DependsOn` appears on both types and methods. It is used to make bootstrap deterministic when a component/factory requires another component first.

If you add a new dependency cycle, expect bootstrap failures or unresolved components. Prefer simplifying dependencies over adding more ordering metadata.

## Defaults Registered By Framework Config

`DefaultConfiguration` registers important shared defaults, including:

- Gson
- JSON `ObjectMapper`
- XML `ObjectMapper`
- HTTP/socket helpers
- executor service
- virtual-thread-aware support when runtime permits

These defaults are used by wrappers such as `RoutingContextLcxWrapper`, which obtains Gson through `ClassPool`.

## Component Indexes

`DIScanner`:

- supports wildcard annotation processing
- filters root elements for `@Component`
- writes `META-INF/class-index-{UUID}.json`

Generated class indexes are resources, not Java source files. If component scanning misses a generated/indexed component, check annotation processing and classpath resources first.

## Tests

Relevant tests live under `common-lib/src/test/java/vn/io/lcx/common/config` and scanner-related packages.

Run:

```bash
mvn -pl common-lib test
```

For DI changes that affect generated components, also compile a downstream example.
