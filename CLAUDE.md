# CLAUDE.md - jlcx-lib Project Guide

## Project Overview

**jlcx-lib** is a reactive Java microservices toolkit built on Vert.x 5.0.8.
It provides a lightweight DI container, annotation-driven HTTP routing with
compile-time code generation, multi-database ORM support, and a rich set of utilities.

| Property      | Value                    |
|---------------|--------------------------|
| GroupId       | `vn.com.lcx`             |
| ArtifactId    | `lcx-lib`                |
| Version       | `4.0.2.lcx-SNAPSHOT`     |
| Java          | 17                       |
| Build Tool    | Maven 3.9+               |

## Module Structure

```
jlcx-lib/
├── common-lib/          Core library (DI, HTTP, database, utilities) — 284 classes
├── processor/           Annotation processors (depends on common-lib)
├── examples/
│   ├── todo-app-example/
│   ├── hibernate-reactive-example/
│   └── grpc-example/    (grpc-client + grpc-server)
└── docs/                Detailed documentation (START HERE)
```

## Documentation

Read these files for in-depth understanding of the codebase:

| Document | What it covers |
|----------|----------------|
| [docs/project-overview.md](docs/project-overview.md) | Architecture, technology stack, all modules, configuration |
| [docs/classpool-di-container.md](docs/classpool-di-container.md) | `ClassPool` DI container, `@Component`, `@Instance`, `@Qualifier`, lifecycle |
| [docs/vertx-web-framework.md](docs/vertx-web-framework.md) | HTTP routing, `@Controller`/`@RestController`, request binding, validation, middleware |
| [docs/database-layer.md](docs/database-layer.md) | JDBC, entity annotations, DDL generation, JPA repositories, reactive repositories, pagination |
| [docs/annotation-processors.md](docs/annotation-processors.md) | `@MapperClass` processor, `@Mapping`/`@Merging`, all 9 processor cross-references |
| [docs/utilities.md](docs/utilities.md) | 30+ utility classes, constants, custom exceptions, package scanner |

## Key Conventions

- **Async-first**: Controller methods return `Future<T>`. Non-blocking I/O via Vert.x event loop.
- **Compile-time code generation**: Annotation processors generate routing, repository, mapper code. No runtime reflection for route discovery.
- **DI**: `ClassPool` — lightweight two-phase dependency injection. Use `@Component`, `@Instance`, `@Qualifier`, `@DependsOn`.
- **Multi-database**: Strategy pattern for Oracle, PostgreSQL, MySQL, SQL Server. Dual sync (Hibernate ORM) and async (Hibernate Reactive / Vert.x SQL clients).
- **Configuration**: `application.yaml` with `${ENV_VAR:default}` syntax. Access via `CommonConstant.applicationConfig`.

## Build

```bash
mvn clean install          # Full build with annotation processing (-proc:full)
```

## Important Packages

| Package | Purpose |
|---------|---------|
| `vn.com.lcx.common.config` | `ClassPool` DI container |
| `vn.com.lcx.common.annotation` | DI and entity annotations |
| `vn.com.lcx.common.database` | JDBC execution, DDL strategies |
| `vn.com.lcx.common.utils` | 30+ utility classes |
| `vn.com.lcx.jpa` | JPA/Hibernate ORM layer, repositories |
| `vn.com.lcx.reactive` | Hibernate Reactive + Vert.x SQL clients |
| `vn.com.lcx.vertx` | Vert.x web framework, controllers, validation |
| `vn.com.lcx.processor` | Annotation processor implementations |

## 9 Annotation Processors

| Processor | Triggers On | Generates |
|-----------|-------------|-----------|
| `ControllerProcessor` | `@Controller`, `@VertxApplication`, `@ContextHandler` | `ApplicationVerticle` with routing |
| `RestControllerProcessor` | `@RestController` | `Reactive{Name}` wrapper |
| `RepositoryProcessor` | `@Repository` (extends `JpaRepository`) | `{Name}Proxy` |
| `HRRepositoryProcessor` | `@HRRepository` (extends `HReactiveRepository`) | `{Name}Impl` |
| `ReactiveRepositoryProcessor` | `@RRepository` (extends `ReactiveRepository`) | `{Name}Impl` |
| `ServiceProcessor` | `@Service` | `{Name}Proxy` with transactions |
| `MapperClassProcessor` | `@MapperClass` | `{Name}Impl` object mapper |
| `SQLMappingProcessor` | `@SQLMapping` | `{Name}Utils` + `{Name}MappingImpl` |
| `DIScanner` | `@Component` (wildcard) | `META-INF/class-index-*.json` |
