# jlcx-lib Agent Guide

This file is also exposed as `AGENTS.md` and `GEMINI.md` through symlinks.
Keep it short. Put domain details in `docs/` and read them on demand.

## Project Facts

`jlcx-lib` is a Java 17 / Maven reactive microservice toolkit.

Core capabilities:

- lightweight DI container in `ClassPool`
- annotation-driven Vert.x HTTP routing
- compile-time code generation through 9 annotation processors
- sync JDBC helpers, JPA repositories, Vert.x SQL repositories, and Hibernate Reactive repositories
- database DDL/query helpers for Oracle, PostgreSQL, MySQL, and SQL Server
- object mapper generation through `@MapperClass`, `@Mapping`, `@Mappings`, and `@Merging`
- shared infrastructure utilities: cache, mail, cron, lock, task retry, logging, auth context, crypto, JSON/YAML, file, date/time

Dependency versions drift. Treat `pom.xml` as the source of truth. At this refresh the source uses Java 17, Vert.x 5.1.2, Hibernate ORM 7.4.1.Final, and Hibernate Reactive 4.4.1.Final.

## Repository Shape

| Path | Current role |
|---|---|
| `common-lib/` | Main library. Contains `vn.io.lcx.common.*`, `vn.io.lcx.jpa.*`, `vn.io.lcx.reactive.*`, `vn.io.lcx.vertx.base.*`, and the real `vn.io.lcx.processor.*` implementations. |
| `processor/` | Annotation-processor facade jar. It contains `ProcessorModule` and `META-INF/services/javax.annotation.processing.Processor`; processor implementations are loaded from transitive `common-lib`. |
| `examples/todo-app-example/` | Reactive SQL Todo app with Vert.x backend, React/Vite frontend, Dockerfile, and Helm chart. |
| `examples/hibernate-reactive-example/` | Todo app using Hibernate Reactive repositories plus React/Vite frontend. |
| `examples/grpc-example/` | gRPC client/server example with shared proto generation scripts. |
| `docs/` | Source-aware domain documentation for agents. |

Root Maven modules are only `common-lib` and `processor`. Examples are independent projects under `examples/`.

## Read-On-Demand Map

Read the narrow doc before editing that area:

| Work area | Read |
|---|---|
| Architecture, modules, dependencies, source map | `docs/project-overview.md` |
| DI, lifecycle, `ClassPool`, `@Component`, `@Instance`, `@Qualifier`, `@DependsOn`, `@PostConstruct` | `docs/classpool-di-container.md` |
| HTTP routing, controllers, request binding, auth/API key, validation, Vert.x wrappers | `docs/vertx-web-framework.md` |
| JDBC, DDL, entity annotations, JPA, reactive SQL, Hibernate Reactive, pagination, specifications | `docs/database-layer.md` |
| Annotation processors, generated classes, templates, processor facade module | `docs/annotation-processors.md` |
| Utilities, cache, mail, cron, locks, tasks, logging, constants, exceptions | `docs/utilities.md` |
| Example apps, frontend stacks, routes, Docker/Helm, gRPC generation | `docs/examples.md` |
| Config keys and environment placeholders | `docs/configuration.md` |
| Verification commands and test layout | `docs/testing-guide.md` |

## Build And Verify

Use targeted verification when possible:

```bash
mvn -pl common-lib test
mvn -pl processor test
mvn clean install
```

Root scripts:

```bash
./build.sh
./clean.sh
./snapshot.sh
./release.sh
```

The parent compiler config uses full annotation processing. If generated-code behavior changes, verify both `common-lib` processor tests and a downstream example compile.

## Source Conventions

- Match existing package names exactly, including existing typos such as `respository`.
- Do not move processor implementations unless the task explicitly asks. They currently live in `common-lib/src/main/java/vn/io/lcx/processor`.
- Generated code relies on templates in `common-lib/src/main/resources/template`; keep processor classpath/resource behavior in mind.
- Controllers and repositories are async-first: Vert.x APIs generally return `Future<T>`.
- Generated routing is compile-time, not runtime route discovery.
- DI injection is constructor/factory-parameter based; fields are used as metadata for matching, not as general reflective field injection.
- Keep docs token-light here; add detail to a specific `docs/*.md` file and link it from this guide.

## Current Gotchas

- `processor/` is a facade/registrar module, not the implementation module.
- `DIScanner` supports wildcard processing and emits `META-INF/class-index-{UUID}.json` for `@Component` classes, but current runtime scanning does not read those index files.
- `@ComponentScan` is read by runtime deployment bootstrap, not by `ControllerProcessor`.
- `@APIKey` currently works through generated route code for direct `@Controller` methods; `RestControllerProcessor` does not copy it from `@RestController` methods.
- Framework config keys include the `server.` prefix in source (`server.database.*`, `server.reactive.database.*`, `server.hreactive.database.*`) even when example YAML nesting makes this easy to miss.
- The Todo example frontend env sample uses `api/v1`, while backend routes are `/api/v2/...`; align env manually when running it.
- The Todo Helm chart uses `DATABASE_*` names, while app config expects `REACTIVE_DATABASE_*`; treat deploy values as example material, not guaranteed production-ready config.
- The Todo Dockerfile currently uses a Java 11 base image although the project targets Java 17.
- Hibernate Reactive example `persistence.xml` appears stale: it lists `Author`/`Book`, while current source has `UsersEntity`/`TasksEntity`.
- gRPC example source uses port `7070`; older README notes may mention `9090`.
- Example resources contain hardcoded defaults and RSA keys for local/demo use. Do not present them as production-safe.
