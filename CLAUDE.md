# jlcx-lib Agent Guide

This file is the source of truth for agent routing. `AGENTS.md` and `GEMINI.md` are symlinks to this file.
Keep this guide short; put durable detail in `docs/` or `docs/agent/`.

## Start Here

- Use Serena for Java symbol discovery when source structure matters.
- Use context-mode for large command output, broad searches, generated analysis, logs, and web fetches.
- Use `rtk` for real shell commands when available; avoid dumping large raw output into chat.
- For code changes, read the narrow domain doc first, then inspect the live symbols/files.
- Treat `pom.xml` as the dependency/version source of truth.

## Project Map

| Path | Current role | Read first |
|---|---|---|
| `common-lib/` | Main Java 17 library: DI, Vert.x HTTP framework, database layers, utilities, and real annotation processor implementations. | `docs/project-overview.md`, then the task-specific doc below. |
| `processor/` | Annotation-processor facade jar with SPI registration; implementations are loaded from transitive `common-lib`. | `docs/annotation-processors.md` |
| `examples/todo-app-example/` | Vert.x reactive SQL Todo backend, React/Vite frontend, Dockerfile, and Helm chart. | `docs/examples.md`, `docs/agent/project-map.md` |
| `examples/hibernate-reactive-example/` | Hibernate Reactive Todo backend plus React/Vite frontend. | `docs/examples.md`, `docs/database-layer.md` |
| `examples/grpc-example/` | gRPC client/server example and proto generation scripts. | `docs/examples.md`, `docs/agent/workflows.md` |
| `docs/` | Domain documentation for framework areas. | Read on demand from the map below. |
| `docs/agent/` | Agent-focused routing, workflows, and operations notes. | `docs/agent/project-map.md` |

Root Maven modules are only `common-lib` and `processor`. Examples are independent projects under `examples/`.

## Common Workflows

Use targeted verification when possible:

```bash
mvn -pl common-lib test
mvn -pl processor test
mvn clean install
```

Focused tests usually use:

```bash
mvn -pl common-lib -Dtest=ClassNameTest test
```

Root scripts include Unix/macOS shell scripts and Windows PowerShell scripts:

```bash
./build.sh
./clean.sh
./snapshot.sh
./release.sh
./osx-build.sh
./build.ps1
./clean.ps1
./deploy.ps1
./snapshot.ps1
./release.ps1
```

See `docs/agent/workflows.md` for example app commands, frontend checks, gRPC generation, and release/deploy script routing. No checked-in CI workflow exists in this repo; use local Maven/pnpm checks.

## Task Routing

Read the narrow doc before editing that area:

| Work area | Read |
|---|---|
| Architecture, modules, dependencies, source map | `docs/project-overview.md`, `docs/agent/project-map.md` |
| DI, lifecycle, `ClassPool`, `@Component`, `@Instance`, `@Qualifier`, `@DependsOn`, `@PostConstruct` | `docs/classpool-di-container.md` |
| HTTP routing, controllers, request binding, auth/API key, validation, Vert.x wrappers | `docs/vertx-web-framework.md` |
| JDBC, DDL, entity annotations, JPA, reactive SQL, Hibernate Reactive, pagination, specifications | `docs/database-layer.md` |
| Annotation processors, generated classes, templates, processor facade module | `docs/annotation-processors.md` |
| Utilities, cache, mail, cron, locks, tasks, logging, constants, exceptions | `docs/utilities.md` |
| Example apps, frontend stacks, routes, Docker/Helm, gRPC generation | `docs/examples.md`, `docs/agent/project-map.md`, `docs/agent/operations.md` |
| Config keys, environment placeholders, demo secrets, logging config | `docs/configuration.md`, `docs/agent/operations.md` |
| Release/deploy scripts and operational examples | `docs/agent/workflows.md`, `docs/agent/operations.md`, then inspect the exact script/config |
| Verification commands and test layout | `docs/testing-guide.md`, `docs/agent/workflows.md` |

## Conventions And Guardrails

- Match existing package names exactly, including existing typos such as `respository`.
- Do not move processor implementations unless explicitly asked. They live in `common-lib/src/main/java/vn/io/lcx/processor`.
- Generated code relies on templates in `common-lib/src/main/resources/template`; keep processor classpath/resource behavior in mind.
- Do not edit `target/`, Maven generated output, frontend build output, or generated gRPC stubs unless explicitly requested.
- For gRPC contract changes, edit `examples/grpc-example/proto/hello.proto` first and regenerate.
- Controllers and repositories are async-first: Vert.x APIs generally return `Future<T>`.
- Generated routing is compile-time, not runtime route discovery.
- DI injection is constructor/factory-parameter based; fields are metadata for matching, not general reflective field injection.
- Code comments must be short and concise.
- Example resources contain hardcoded defaults and RSA keys for local/demo use only.

## Current Gotchas

- `processor/` is a facade/registrar module, not the implementation module.
- `DIScanner` supports wildcard processing and emits `META-INF/class-index-{UUID}.json` for `@Component` classes, but current runtime scanning does not read those index files.
- `@ComponentScan` is read by runtime deployment bootstrap, not by `ControllerProcessor`.
- `@APIKey` currently works through generated route code for direct `@Controller` methods; `RestControllerProcessor` does not copy it from `@RestController` methods.
- Framework config keys include the `server.` prefix in source (`server.database.*`, `server.reactive.database.*`, `server.hreactive.database.*`) even when example YAML nesting makes this easy to miss.
- The Todo example frontend env sample uses `api/v1`, while backend routes are `/api/v2/...`; align env manually when running it.
- The Todo Helm chart uses `DATABASE_*` names, while app config expects `REACTIVE_DATABASE_*`; treat deploy values as example material, not guaranteed production-ready config.
- The Todo Dockerfile currently uses a Java 11 base image although the project targets Java 17.
- `examples/hibernate-reactive-example/src/main/resources/META-INF/persistence.xml` appears stale: it lists `Author`/`Book`, while current source has `UsersEntity`/`TasksEntity`.
- gRPC example source uses port `7070`, while `examples/grpc-example/README.md` still mentions `9090`.
