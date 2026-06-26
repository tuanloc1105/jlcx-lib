# Project Overview

`jlcx-lib` is a Java 17 toolkit for building reactive Vert.x services with a small DI container, compile-time code generation, JDBC/JPA/reactive database helpers, and common infrastructure utilities.

This document is the high-level map. Read narrower docs before editing a domain:

- DI: `docs/classpool-di-container.md`
- HTTP: `docs/vertx-web-framework.md`
- Database: `docs/database-layer.md`
- Processors: `docs/annotation-processors.md`
- Utilities: `docs/utilities.md`
- Examples: `docs/examples.md`
- Config: `docs/configuration.md`
- Tests: `docs/testing-guide.md`

## Modules

| Module/path | Purpose |
|---|---|
| `common-lib` | Main library and most source code. It contains common utilities, DI/config, database layers, JPA contracts, reactive repositories, Vert.x web framework, and processor implementations. |
| `processor` | Annotation-processor facade jar. It registers processor class names through Java SPI and depends on `common-lib`, where the processor implementations live. |
| `examples/todo-app-example` | Reactive SQL Todo app with backend, React/Vite frontend, Dockerfile, and Helm chart. |
| `examples/hibernate-reactive-example` | Hibernate Reactive Todo app with backend and React/Vite frontend. |
| `examples/grpc-example` | gRPC client/server/proto sample with protoc scripts. |

Root Maven modules are `common-lib` and `processor`. Example projects are not root Maven modules.

## Source Package Map

| Package | Role |
|---|---|
| `vn.io.lcx.common.annotation` | DI annotations, entity/table annotations, mapper annotations, SQL mapping annotations. |
| `vn.io.lcx.common.config` | `ClassPool`, default object mappers, Gson, Logback setup, framework defaults. |
| `vn.io.lcx.common.database` | JDBC executor, deprecated connection context, database property model, DDL/entity/query helpers. |
| `vn.io.lcx.common.database.utils` | Entity analysis, SQL generation, and Oracle/PostgreSQL/MySQL/SQL Server DDL strategies. |
| `vn.io.lcx.common.database.pageable` | Cross-database pagination SQL generation. |
| `vn.io.lcx.common.database.specification` | Fluent SQL condition builder. |
| `vn.io.lcx.common.utils` | String, object, JSON/YAML, crypto, date/time, file, random, HTTP, collection, serialization helpers. |
| `vn.io.lcx.common.cache` | Redis pool abstraction and implementation. |
| `vn.io.lcx.common.mail` | Mail config and reactive mail sending. |
| `vn.io.lcx.common.cron` | Cron parser and field model. |
| `vn.io.lcx.common.lock` | Lock manager helpers. |
| `vn.io.lcx.common.thread` / `task` | Executor, virtual-thread support, batch and retry helpers. |
| `vn.io.lcx.common.context` | Request/auth context. |
| `vn.io.lcx.common.logging` | Logback MDC converters for Vert.x trace/operation values. |
| `vn.io.lcx.jpa.*` | JPA annotations, repository contract, entity/session helpers. Note the actual package typo `respository` exists in source. |
| `vn.io.lcx.reactive.*` | Vert.x SQL repository contracts, SQL statement model, reactive transaction/connection/mapping helpers, Hibernate Reactive contract. |
| `vn.io.lcx.vertx.base.*` | HTTP annotations, controller base, wrappers, request/response models, validation, Verticle base. |
| `vn.io.lcx.processor.*` | All 9 annotation processor implementations plus generator models, services, templates, and utilities. |

## Runtime Architecture

Typical app flow:

1. App class uses `@VertxApplication`.
2. Annotation processors generate routing, wrappers, repositories, service proxies, SQL mapping helpers, object mappers, and component metadata files.
3. `MyVertxDeployment` reads `@ComponentScan`, seeds Vert.x defaults, and starts `ClassPool`.
4. `ClassPool` scans packages through `PackageScanner`, registers components/instances, resolves constructor/factory parameters, and runs lifecycle hooks.
5. Generated `ApplicationVerticle` wires Vert.x routes, auth handlers, static resources, and controller wrappers.
6. Controllers delegate to services/repositories that return `Future<T>` for async Vert.x paths.

## Important Generated Outputs

| Trigger | Generated output |
|---|---|
| `@VertxApplication`, `@Controller`, `@ContextHandler` | Fixed `vn.io.lcx.vertx.verticle.ApplicationVerticle` |
| `@RestController` | `Reactive{OriginalClass}` wrapper extending `ReactiveController` |
| `@Repository` | `{RepositoryInterface}Proxy` |
| `@Service` | `{ServiceClass}Proxy` |
| `@RRepository` | `{RepositoryInterface}Impl` |
| `@HRRepository` | `{RepositoryInterface}Impl` |
| `@MapperClass` | `{MapperInterface}Impl` |
| `@SQLMapping` | `{Entity}Utils` and `{Entity}MappingImpl` |
| `@Component` scanned by `DIScanner` | `META-INF/class-index-{UUID}.json` |

Current runtime DI does not read the `META-INF/class-index-*` files; package scanning remains the authoritative discovery path.

## Resources

`common-lib/src/main/resources` contains:

- `default-banner.txt`
- `default-logback.xml`
- generator templates under `template/` for controller, verticle, repository, service, SQL mapping, entity mapping, JPA method/do-work, and criteria handling

There is no main `application.yaml` in `common-lib`; app configs live in examples/tests.

## Dependency Source Of Truth

Do not hardcode exact dependency versions into docs unless needed for a migration note. Use Maven POMs instead: root `pom.xml` for shared versions, plus the relevant module or example `pom.xml` for local overrides.

## Editing Guidance

- Keep changes surgical. Most framework behavior is compile-time generated; inspect processors/templates before changing annotations.
- Prefer existing helper APIs over new abstractions.
- Update the narrow doc when changing behavior. Keep `AGENTS.md`/`CLAUDE.md` as a routing index.
- For generated-code changes, verify with processor tests and at least one downstream compile.
