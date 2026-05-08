# Testing Guide

Use the smallest verification that covers the changed behavior. Generated-code changes need broader checks than normal utility changes.

## Main Commands

```bash
mvn -pl common-lib test
mvn -pl processor test
mvn clean install
```

Root scripts:

```bash
./build.sh
./clean.sh
```

The root Maven build covers `common-lib` and `processor`. Examples are separate projects under `examples/`.

## Test Layout

Most tests live under `common-lib/src/test/java`.

Major areas:

- `vn.io.lcx.common.config`: `ClassPool` and default config behavior
- `vn.io.lcx.common.database`: executor, database property, entity analysis, DDL strategies
- `vn.io.lcx.common.database.pageable`: DB-specific pagination
- `vn.io.lcx.common.database.specification`: fluent SQL condition builder
- `vn.io.lcx.common.utils`: string/object/JSON/YAML/crypto/date/file/random utilities
- `vn.io.lcx.reactive`: SQL statements, entity mapping, reactive helpers
- `vn.io.lcx.vertx.base`: controller helpers, wrappers, validation, request/response behavior
- `vn.io.lcx.processor`: processor templates, mapper metadata, generator utilities

Processor tests are in `common-lib`, not in `processor`.

## What To Run By Change Type

| Change | Suggested verification |
|---|---|
| Utility-only change | Relevant `common-lib` test class, then `mvn -pl common-lib test` if shared. |
| DI lifecycle change | `ClassPool` tests and any component scan/index tests. |
| Database strategy/pageable/specification change | Specific DB strategy/pageable/spec tests plus `mvn -pl common-lib test`. |
| Reactive repository/query change | Reactive SQL/entity mapping tests, then compile an example that uses the path. |
| Vert.x controller/request/validation change | Vert.x base tests and an example compile. |
| Processor/template change | Processor tests in `common-lib`, inspect generated sources, then `mvn clean install`. |
| Processor facade/SPI change | `mvn -pl processor test`, then downstream compile with annotation processing enabled. |
| Example frontend change | `pnpm run lint` and `pnpm run build` inside the specific `web/` folder. |
| Example backend change | Run that example's Maven build script or `mvn clean package` inside the example. |

## Generated Code Checks

For processors:

1. Run focused tests first.
2. Compile a downstream target with annotation processing enabled.
3. Inspect `target/generated-sources/annotations`.
4. Check that generated class/package names match docs.
5. If ServiceLoader fails, verify `processor` SPI plus transitive `common-lib` on annotation processor path.

Generated outputs to expect:

- `ApplicationVerticle`
- `Reactive{Controller}`
- `{Repository}Proxy`
- `{Service}Proxy`
- `{RRepository}Impl`
- `{HRRepository}Impl`
- `{Mapper}Impl`
- `{Entity}Utils`
- `{Entity}MappingImpl`
- `META-INF/class-index-{UUID}.json`

## Example Verification

Todo app:

```bash
cd examples/todo-app-example
./build.sh
cd web && pnpm install && pnpm run build
```

Hibernate Reactive example:

```bash
cd examples/hibernate-reactive-example
./build.sh
cd web && pnpm install && pnpm run build
```

gRPC example:

```bash
cd examples/grpc-example
./build.sh
mvn clean compile
```

`grpc-example/build.sh` needs external protoc plugins from `DEV_KIT_LOCATION`.

## Known Test/Build Caveats

- Example apps may require local PostgreSQL or matching env vars.
- Example resources contain demo keys/defaults; do not use them for production assertions.
- Hibernate Reactive example persistence metadata appears stale against current entities.
- Todo deploy chart env names may not match app YAML keys.
