# jlcx-lib - Project Overview

## Introduction

**jlcx-lib** is a reactive Java microservices toolkit built on top of Vert.x. It provides a
lightweight dependency injection container, annotation-driven HTTP routing with compile-time
code generation, multi-database ORM support, and a rich set of utilities.

| Property      | Value                                 |
|---------------|---------------------------------------|
| GroupId       | `vn.io.lcx`                          |
| ArtifactId    | `lcx-lib`                             |
| Version       | `4.0.3.lcx`                  |
| Java Version  | 17                                    |
| Build Tool    | Maven 3.9+                            |

---

## Modules

The project is a Maven multi-module build with two published modules and three example
applications.

```
jlcx-lib/
├── common-lib/          Main library - DI, HTTP, database, utilities
├── processor/           Annotation processor module (depends on common-lib)
├── examples/
│   ├── todo-app-example/
│   ├── hibernate-reactive-example/
│   └── grpc-example/
└── docs/                Documentation
```

### common-lib

The core module containing all runtime code:

| Package                         | Description                                    |
|---------------------------------|------------------------------------------------|
| `vn.io.lcx.common.annotation` | DI and mapping annotations                     |
| `vn.io.lcx.common.config`     | `ClassPool` DI container, configuration        |
| `vn.io.lcx.common.scanner`    | Runtime package scanning (`PackageScanner`)    |
| `vn.io.lcx.common.database`   | JDBC execution, strategies, entity analysis    |
| `vn.io.lcx.common.database.context` | `ConnectionContext` (deprecated)          |
| `vn.io.lcx.common.utils`      | 30+ utility classes                            |
| `vn.io.lcx.common.constant`   | Global constants                               |
| `vn.io.lcx.common.exception`  | Custom exceptions                              |
| `vn.io.lcx.common.cache`      | Caching abstractions                           |
| `vn.io.lcx.common.task`       | Task execution, batch processing, retry logic  |
| `vn.io.lcx.common.thread`     | Thread utilities                               |
| `vn.io.lcx.common.lock`       | Locking mechanisms                             |
| `vn.io.lcx.common.mail`       | Email utilities                                |
| `vn.io.lcx.common.cron`       | Scheduled task / cron support                  |
| `vn.io.lcx.common.logging`    | Logging configuration                          |
| `vn.io.lcx.common.dto`        | Shared DTOs                                    |
| `vn.io.lcx.common.context`    | Context management (`AuthContext`)              |
| `vn.io.lcx.common.array`      | `LargeArray<T>` - chunked large collections    |
| `vn.io.lcx.common.ref`        | `Ref<T>` - mutable reference wrapper           |
| `vn.io.lcx.common.javaassist` | Bytecode manipulation via Javassist            |
| `vn.io.lcx.jpa`               | JPA/Hibernate ORM layer                        |
| `vn.io.lcx.jpa.dto`           | `BaseEntityDTO`, `BaseUnixEntityDTO`           |
| `vn.io.lcx.jpa.functional`    | `RowMapper`, `BatchCallback`, `ResultBatchCallback` |
| `vn.io.lcx.reactive`          | Hibernate Reactive integration                 |
| `vn.io.lcx.reactive.functional`| `RowBatchCallback<T,U>` async batch callback  |
| `vn.io.lcx.reactive.wrapper`  | `PreparedQueryWrapper`, `PoolLcxWrapper`       |
| `vn.io.lcx.reactive.utils`    | `ReactiveRowStreamingUtils`, reactive `FileUtils` |
| `vn.io.lcx.vertx`             | Vert.x web framework base classes              |
| `vn.io.lcx.vertx.base.utils`  | `VertxSocketClientUtils`, `VertxWebClientHttpUtils` |
| `vn.io.lcx.processor`         | Annotation processor implementations           |

### processor

A thin module that registers annotation processors via
`META-INF/services/javax.annotation.processing.Processor`. It depends on `common-lib` at
compile scope. The nine registered processors are:

| Processor                      | Annotation Target          | Output                                |
|--------------------------------|----------------------------|---------------------------------------|
| `ControllerProcessor`          | `@Controller`, `@VertxApplication`, `@ContextHandler` | `ApplicationVerticle` with routing |
| `RestControllerProcessor`      | `@RestController`          | Reactive controller wrapper           |
| `RepositoryProcessor`          | Repository interfaces      | JDBC repository implementations       |
| `HRRepositoryProcessor`        | HR repository interfaces   | Hibernate Reactive repository impls   |
| `ReactiveRepositoryProcessor`  | Reactive repository ifaces | Vert.x reactive repository impls      |
| `ServiceProcessor`             | Service interfaces         | Service proxies with transactions     |
| `MapperClassProcessor`         | `@MapperClass`             | Object mapper implementations         |
| `SQLMappingProcessor`          | `@SQLMapping`              | Entity utils + `EntityMapping` impls  |
| `DIScanner`                    | DI-related annotations     | Component scanning helpers            |

---

## Technology Stack

### Core Runtime

| Technology            | Version   | Purpose                                      |
|-----------------------|-----------|----------------------------------------------|
| Vert.x                | 5.0.8     | Async event loop, HTTP server, SQL clients   |
| Hibernate ORM         | 7.2.6     | JPA persistence (sync)                       |
| Hibernate Reactive    | 4.2.5     | Non-blocking persistence                     |
| HikariCP              | 7.0.2     | JDBC connection pooling                      |
| Gson                  | 2.13.2    | JSON serialization/deserialization           |
| Jackson               | 2.21.1    | JSON/XML data binding (5 modules)            |
| SnakeYAML             | 2.6       | YAML configuration loading                   |
| SLF4J + Logback       | 2.0.17 / 1.5.32 | Logging                               |
| Javassist             | 3.30.2    | Bytecode manipulation                        |
| Lombok                | 1.18.44   | Boilerplate reduction                        |
| Apache Commons Text   | 1.15.0    | Text manipulation and interpolation          |
| Apache Commons Lang3  | 3.20.0    | String utilities, reflection helpers         |
| Apache Commons Collections4 | 4.5.0 | Advanced collection operations             |
| JAXB API + Runtime    | 4.0.5 / 4.0.6 | XML binding                             |
| Jakarta Persistence API | 3.2.0   | JPA specification                            |
| Jakarta Annotation API | 3.0.0    | Annotation processing                        |

### Database Drivers

| Database              | Driver Version  |
|-----------------------|-----------------|
| Oracle                | ojdbc11 23.26.1 |
| PostgreSQL            | 42.7.10         |
| MySQL                 | 9.6.0           |
| SQL Server            | 13.4.0          |

### Messaging & Caching

| Technology            | Version   |
|-----------------------|-----------|
| Apache Kafka          | 4.2.0     |
| Jedis (Redis)         | 7.4.0     |
| Ehcache               | 3.11.1    |

### gRPC

| Technology            | Version   |
|-----------------------|-----------|
| gRPC (Netty shaded)   | 1.79.0    |
| Protobuf              | 4.34.0    |

### Monitoring

| Technology                    | Version   |
|-------------------------------|-----------|
| Micrometer Core               | 1.16.4    |
| Micrometer Prometheus Registry| 1.16.4    |
| Dropwizard Metrics 4          | 4.2.38    |
| Dropwizard Metrics 5          | 5.0.6     |

### Security

| Technology            | Version   |
|-----------------------|-----------|
| Vert.x Auth JWT       | 5.0.8     |
| jBCrypt               | 0.4       |
| Jakarta Mail           | 2.0.5     |

### Testing

| Technology            | Version   |
|-----------------------|-----------|
| JUnit Jupiter         | 6.0.3     |
| Mockito               | 5.23.0    |
| Mockito Inline        | 5.2.0     |
| DataFaker             | 2.5.4     |
| H2 Database           | 2.4.240   |
| Vert.x Unit           | 5.0.8     |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  Application Code                    │
│  @Controller  @Component  @RestController  @Entity   │
└──────────┬─────────────┬─────────────┬──────────────┘
           │             │             │
    ┌──────▼──────┐ ┌────▼────┐ ┌─────▼──────┐
    │ Vert.x Web  │ │ClassPool│ │  Database   │
    │  Framework  │ │   DI    │ │   Layer     │
    │ (generated) │ │Container│ │ (ORM/JDBC)  │
    └──────┬──────┘ └────┬────┘ └─────┬──────┘
           │             │             │
    ┌──────▼─────────────▼─────────────▼──────┐
    │            Vert.x Core 5.0.8             │
    │     Event Loop  │  Worker Pool           │
    └──────────────────┬───────────────────────┘
                       │
    ┌──────────────────▼───────────────────────┐
    │        Infrastructure Services            │
    │  Redis │ Kafka │ Mail │ Metrics │ gRPC   │
    └──────────────────────────────────────────┘
```

### Key Design Principles

1. **Compile-time code generation** - Annotation processors generate routing, repository, and
   mapper code at build time. No runtime reflection for route discovery.

2. **Async-first** - All controller methods return `Future<T>`. Non-blocking I/O through the
   Vert.x event loop.

3. **Lightweight DI** - `ClassPool` provides a simple two-phase dependency injection container
   without the overhead of Spring or CDI. See [classpool-di-container.md](classpool-di-container.md).

4. **Multi-database** - Strategy pattern for DDL generation across Oracle, PostgreSQL, MySQL,
   and SQL Server. Dual sync (Hibernate ORM) and async (Hibernate Reactive / Vert.x SQL clients)
   data access.

5. **Convention over configuration** - Sensible defaults with YAML configuration override via
   `application.yaml`.

---

## Configuration

The framework loads `application.yaml` from the classpath. Properties are accessed via
`CommonConstant.applicationConfig` (an `LCXProperties` instance).

Environment variable substitution is supported:

```yaml
server:
  port: ${SERVER_PORT:8080}
  enable-http-2: false
  enable-metrics: false
  enable-virtual-thread: false
  body-bytes-limit: 10485760
  api-key: ${API_KEY:}

database:
  connection-string: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
  username: ${DB_USER:postgres}
  password: ${DB_PASS:secret}
  driver-class-name: org.postgresql.Driver
  initial-pool-size: 5
  max-pool-size: 20
```

---

## Build & Scripts

| Script         | Purpose                            |
|----------------|------------------------------------|
| `build.sh/ps1` | Full Maven build                   |
| `clean.sh/ps1` | Clean build artifacts              |
| `snapshot.sh/ps1` | Deploy snapshot to repository   |
| `release.sh/ps1`  | Deploy release to repository    |

Build command:

```bash
mvn clean install
```

The `-proc:full` compiler argument ensures annotation processors run during compilation.

---

## Related Documentation

| Document                                                  | Description                   |
|-----------------------------------------------------------|-------------------------------|
| [classpool-di-container.md](classpool-di-container.md)    | DI container reference        |
| [vertx-web-framework.md](vertx-web-framework.md)         | HTTP framework & routing      |
| [database-layer.md](database-layer.md)                    | Database & ORM reference      |
| [utilities.md](utilities.md)                              | Utility classes reference     |
