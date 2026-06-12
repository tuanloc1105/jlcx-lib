# jlcx-lib

A reactive Java microservices toolkit built on Vert.x 5.1.2. It provides a lightweight DI container (`ClassPool`), annotation-driven HTTP routing with compile-time code generation, multi-database ORM support, and a rich set of utilities — so teams can ship production-ready microservices without re-implementing infrastructure pieces.

| Property   | Value                  |
| ---------- | ---------------------- |
| GroupId    | `vn.io.lcx`          |
| ArtifactId | `lcx-lib`            |
| Version    | `4.0.5.lcx-SNAPSHOT` |
| Java       | 17                     |
| Build Tool | Maven 3.9+             |
| License    | Apache 2.0             |

## Highlights

- **Compile-time code generation** — Annotation processors generate routing, repository, service proxy, and mapper code at build time. No runtime reflection for route discovery.
- **Async-first** — Controller methods return `Future<T>`. Non-blocking I/O via the Vert.x event loop.
- **Lightweight DI** — `ClassPool` two-phase dependency injection container with `@Component`, `@Instance`, `@Qualifier`, `@DependsOn`.
- **Multi-database ORM** — Strategy pattern supporting Oracle, PostgreSQL, MySQL, and SQL Server. Dual sync (Hibernate ORM 7.4.1.Final) and async (Hibernate Reactive 4.4.1.Final / Vert.x SQL clients) paths.
- **Rich utilities** — Shared helpers and infrastructure for caching, task scheduling, retry logic, mail, cron, locking, large collections, crypto, JSON/YAML, file handling, and more.
- **End-to-end examples** — gRPC, Hibernate Reactive, and Todo app examples that double as documentation and integration tests.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Application Code                   │
│  @Controller  @Component  @RestController  @Entity  │
└──────────┬─────────────┬─────────────┬──────────────┘
           │             │             │
    ┌──────▼──────┐ ┌────▼────┐ ┌─────▼──────┐
    │ Vert.x Web  │ │ClassPool│ │  Database  │
    │  Framework  │ │   DI    │ │   Layer    │
    │ (generated) │ │Container│ │ (ORM/JDBC) │
    └──────┬──────┘ └────┬────┘ └─────┬──────┘
           │             │             │
    ┌──────▼─────────────▼─────────────▼──────┐
    │            Vert.x Core 5.1.2            │
    │     Event Loop  │  Worker Pool          │
    └──────────────────┬──────────────────────┘
                       │
    ┌──────────────────▼───────────────────────┐
    │        Infrastructure Services           │
    │  Redis │ Kafka │ Mail │ Metrics │ gRPC   │
    └──────────────────────────────────────────┘
```

## Modules

```
jlcx-lib/
├── common-lib/          Core library (DI, HTTP, database, utilities, processor implementations)
├── processor/           Annotation-processor facade/SPI jar (depends on common-lib)
├── examples/
│   ├── todo-app-example/
│   ├── hibernate-reactive-example/
│   └── grpc-example/    (grpc-client + grpc-server)
└── docs/                Detailed documentation
```

- **`common-lib`** — Core runtime: `ClassPool` DI container, Vert.x web framework base classes, JDBC/reactive database layer, JPA helpers, entity annotations, shared utilities/infrastructure, resource templates, and the real processor implementations.
- **`processor`** — Facade/registrar jar for 9 annotation processors via `META-INF/services/javax.annotation.processing.Processor`; implementation classes and templates are loaded from transitive `common-lib`.
- **`examples`** — Runnable Vert.x projects demonstrating typical usage patterns.

## 9 Annotation Processors

| Processor                       | Triggers On                                                 | Generates                               |
| ------------------------------- | ----------------------------------------------------------- | --------------------------------------- |
| `ControllerProcessor`         | `@Controller`, `@VertxApplication`, `@ContextHandler` | `ApplicationVerticle` with routing    |
| `RestControllerProcessor`     | `@RestController`                                         | `Reactive{Name}` wrapper              |
| `RepositoryProcessor`         | `@Repository` (extends `JpaRepository`)                 | `{Name}Proxy`                         |
| `HRRepositoryProcessor`       | `@HRRepository` (extends `HReactiveRepository`)         | `{Name}Impl`                          |
| `ReactiveRepositoryProcessor` | `@RRepository` (extends `ReactiveRepository`)           | `{Name}Impl`                          |
| `ServiceProcessor`            | `@Service`                                                | `{Name}Proxy` with transactions       |
| `MapperClassProcessor`        | `@MapperClass`                                            | `{Name}Impl` object mapper            |
| `SQLMappingProcessor`         | `@SQLMapping`                                             | `{Name}Utils` + `{Name}MappingImpl` |
| `DIScanner`                   | `@Component` (wildcard)                                   | `META-INF/class-index-*.json`         |

## Tech Stack

### Core Runtime

| Technology                                 | Version           | Purpose                                                                       |
| ------------------------------------------ | ----------------- | ----------------------------------------------------------------------------- |
| Vert.x                                     | 5.1.2             | Async event loop, HTTP server, SQL clients, gRPC, Redis, Auth JWT, Micrometer |
| Hibernate ORM                              | 7.4.1.Final       | JPA persistence (sync)                                                        |
| Hibernate Reactive                         | 4.4.1.Final       | Non-blocking persistence                                                      |
| HikariCP                                   | 7.0.2             | JDBC connection pooling                                                       |
| Jackson                                    | 2.22.0            | JSON/XML data binding                                                         |
| Gson                                       | 2.14.0            | JSON serialization                                                            |
| SnakeYAML                                  | 2.6               | YAML configuration loading                                                    |
| Lombok                                     | 1.18.46           | Boilerplate reduction                                                         |
| Javassist                                  | 3.31.0-GA         | Bytecode manipulation                                                         |
| Apache Commons (Text, Lang3, Collections4) | 1.15 / 3.20 / 4.5 | String, reflection, collection utilities                                      |
| JAXB API + Runtime                         | 4.0.5 / 4.0.9     | XML binding                                                                   |
| Jakarta Persistence API                    | 3.2.0             | JPA specification                                                             |

### Database Drivers

| Database     | Driver Version      |
| ------------ | ------------------- |
| Oracle       | ojdbc11 23.26.2.0.0 |
| PostgreSQL   | 42.7.11             |
| MySQL        | 9.7.0               |
| SQL Server   | 13.4.0.jre11        |
| H2 (testing) | 2.4.240             |

### Messaging & Caching

| Technology    | Version |
| ------------- | ------- |
| Apache Kafka  | 4.3.0   |
| Jedis (Redis) | 7.5.2   |
| Ehcache       | 3.12.0  |

### gRPC

| Technology          | Version |
| ------------------- | ------- |
| gRPC (Netty shaded) | 1.82.0  |
| Protobuf            | 4.35.1  |

### Monitoring

| Technology                   | Version |
| ---------------------------- | ------- |
| Micrometer Core + Prometheus | 1.17.0  |
| Dropwizard Metrics 4         | 4.2.39  |
| Dropwizard Metrics 5         | 5.0.7   |

### Security

| Technology      | Version |
| --------------- | ------- |
| Vert.x Auth JWT | 5.1.2   |
| jBCrypt         | 0.4     |
| Jakarta Mail    | 2.0.5   |

### Testing

| Technology    | Version |
| ------------- | ------- |
| JUnit Jupiter | 6.1.0   |
| Mockito       | 5.23.0  |
| DataFaker     | 2.5.4   |

### Build Tooling

| Technology            | Version    |
| --------------------- | ---------- |
| Maven Compiler Plugin | 3.15.0     |
| Maven Surefire Plugin | 3.5.6      |
| SonarQube Scanner     | 5.7.0.6970 |

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- (Optional) Node.js for the web assets in `examples/todo-app-example/web`

## Installation

To use `jlcx-lib`, pull the source code and build it locally.

### 1. Clone the repository

```bash
git clone https://github.com/tuanloc1105/jlcx-lib.git
cd jlcx-lib
```

### 2. Build and install

Build the project to install the artifacts into your local Maven repository:

```bash
mvn clean install
```

Alternatively, you can use the provided helper scripts:

- **Linux/macOS:**
  ```bash
  ./build.sh
  ```
- **Windows (PowerShell):**
  ```powershell
  .\build.ps1
  ```

> **Note:** The helper scripts assume `JAVA_HOME` and `MAVEN_HOME` are located under `$HOME/dev-kit` (Bash) or define `DEV_KIT_LOCATION` (PowerShell). Override these variables in the scripts if your environment differs.

## Use in your Maven project

Add the core library and annotation processor to your project (adjust the version as needed):

```xml
<dependency>
  <groupId>vn.io.lcx</groupId>
  <artifactId>common-lib</artifactId>
  <version>4.0.5.lcx-SNAPSHOT</version>
</dependency>

<!-- Annotation processor (compile-time) -->
<dependency>
  <groupId>vn.io.lcx</groupId>
  <artifactId>processor</artifactId>
  <version>4.0.5.lcx-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

If you manage versions centrally, declare the coordinates in your BOM or parent POM and omit the `<version>` tags above.

## Annotation Processor Setup

The annotation processor runs during compilation and generates sources based on the annotations and templates in `common-lib`. If your build does not automatically pick it up, wire it into the Maven Compiler Plugin:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.15.0</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>vn.io.lcx</groupId>
        <artifactId>processor</artifactId>
        <version>4.0.5.lcx-SNAPSHOT</version>
      </path>
    </annotationProcessorPaths>
    <compilerArgs>
      <arg>-proc:full</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

Generated sources are emitted under Maven's default `target/generated-sources/annotations` directory.

## Configuration Reference

The library uses configuration keys (typically in `application.yaml`) with `${ENV_VAR:default}` syntax support. Access configuration programmatically via `CommonConstant.applicationConfig`.

### General Configuration

| Key                      | Type    | Description                                                  |
| :----------------------- | :------ | :----------------------------------------------------------- |
| `server.port`          | Integer | The HTTP server port for the application (default:`8080`). |
| `server.enable-http-2` | Boolean | Enable HTTP/2 support for the server (default:`false`).    |

### JDBC Database Configuration (Hibernate)

| Key                                   | Type    | Description                                                         |
| :------------------------------------ | :------ | :------------------------------------------------------------------ |
| `server.database.host`              | String  | Database host address.                                              |
| `server.database.port`              | Integer | Database port.                                                      |
| `server.database.username`          | String  | Database username.                                                  |
| `server.database.password`          | String  | Database password.                                                  |
| `server.database.name`              | String  | Database name.                                                      |
| `server.database.schema_name`       | String  | (Optional) Default schema name.                                     |
| `server.database.type`              | Enum    | Database type:`ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`.     |
| `server.database.driver_class_name` | String  | JDBC driver class name. Defaults based on `server.database.type`. |
| `server.database.initial_pool_size` | Integer | Initial connection pool size (HikariCP `minimumIdle`).            |
| `server.database.max_pool_size`     | Integer | Maximum connection pool size (HikariCP `maximumPoolSize`).        |
| `server.database.max_timeout`       | Integer | Connection timeout in seconds.                                      |

### Reactive Database Configuration (Vert.x SQL Clients)

| Key                                        | Type    | Description                                                     |
| :----------------------------------------- | :------ | :-------------------------------------------------------------- |
| `server.reactive.database.host`          | String  | Reactive database host.                                         |
| `server.reactive.database.port`          | Integer | Reactive database port.                                         |
| `server.reactive.database.username`      | String  | Reactive database username.                                     |
| `server.reactive.database.password`      | String  | Reactive database password.                                     |
| `server.reactive.database.name`          | String  | Reactive database name.                                         |
| `server.reactive.database.max_pool_size` | Integer | Maximum pool size for the reactive client.                      |
| `server.reactive.database.type`          | Enum    | Database type:`ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`. |

### Hibernate Reactive Configuration

| Key                                         | Type    | Description                                                     |
| :------------------------------------------ | :------ | :-------------------------------------------------------------- |
| `server.hreactive.database.host`          | String  | Database host address.                                          |
| `server.hreactive.database.port`          | Integer | Database port.                                                  |
| `server.hreactive.database.username`      | String  | Database username.                                              |
| `server.hreactive.database.password`      | String  | Database password.                                              |
| `server.hreactive.database.name`          | String  | Database name.                                                  |
| `server.hreactive.database.type`          | Enum    | Database type:`ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`. |
| `server.hreactive.database.max_pool_size` | Integer | Maximum connection pool size.                                   |

### Redis Configuration

| Key                                        | Type    | Description                                 |
| :----------------------------------------- | :------ | :------------------------------------------ |
| `server.reactive.redis.host`             | String  | Redis host.                                 |
| `server.reactive.redis.port`             | Integer | Redis port.                                 |
| `server.reactive.redis.password`         | String  | Redis password.                             |
| `server.reactive.database.max_pool_size` | Integer | Reused for Redis pool size (default `5`). |

### Metrics Configuration

| Key                         | Type    | Description                                                            |
| :-------------------------- | :------ | :--------------------------------------------------------------------- |
| `server.metrics.enable`   | Boolean | Enables Vert.x Micrometer metrics (Prometheus).                        |
| `server.metrics.port`     | Integer | Port for the embedded metrics server (default `8081`).               |
| `server.metrics.endpoint` | String  | Endpoint for the embedded metrics server (default `/metrics`).       |
| `server.enable-metrics`   | Boolean | Enables the `/metrics` route handler in the main application router. |

### JSON Configuration

| Key                      | Type | Description                                                              |
| :----------------------- | :--- | :----------------------------------------------------------------------- |
| `json.sensitive_field` | List | Field names to mask/obfuscate when serializing JSON (e.g., for logging). |

## Running the Examples

Each example module can be built independently:

```bash
cd examples/todo-app-example
mvn clean package
```

- **`examples/grpc-example`** — Vert.x gRPC server and client (see its `README.md` for run commands).
- **`examples/hibernate-reactive-example`** — Hibernate Reactive + Vert.x demo with `application.yaml`, `logback.xml`, and `persistence.xml`.
- **`examples/todo-app-example`** — Todo backend built with `common-lib` and `processor`, packaged with the Spring Boot repackage plugin for an executable JAR.

Each example includes `build.sh`, `clean.sh`, and PowerShell counterparts.

## Important Packages

| Package                             | Purpose                                                                   |
| ----------------------------------- | ------------------------------------------------------------------------- |
| `vn.io.lcx.common.config`         | `ClassPool` DI container, configuration                                 |
| `vn.io.lcx.common.annotation`     | DI and entity annotations                                                 |
| `vn.io.lcx.common.database`       | JDBC execution, database property model, pageable/specification contracts |
| `vn.io.lcx.common.database.utils` | Entity analysis, SQL generation, DB-specific DDL strategies               |
| `vn.io.lcx.common.utils`          | 28 utility classes                                                        |
| `vn.io.lcx.common.task`           | Task execution, batch processing, retry logic                             |
| `vn.io.lcx.common.cache`          | Caching abstractions                                                      |
| `vn.io.lcx.common.cron`           | Scheduled task / cron support                                             |
| `vn.io.lcx.common.mail`           | Email utilities                                                           |
| `vn.io.lcx.common.lock`           | Locking mechanisms                                                        |
| `vn.io.lcx.common.context`        | Context management (`AuthContext`)                                      |
| `vn.io.lcx.common.array`          | `LargeArray<T>` — chunked large collections                            |
| `vn.io.lcx.jpa`                   | JPA/Hibernate ORM layer, repositories                                     |
| `vn.io.lcx.reactive`              | Hibernate Reactive + Vert.x SQL clients                                   |
| `vn.io.lcx.vertx`                 | Vert.x web framework, controllers, validation                             |
| `vn.io.lcx.processor`             | Annotation processor implementations                                      |

## Publishing

Use the provided scripts to push artifacts to Nexus (credentials must be configured in your `~/.m2/settings.xml`):

```bash
./snapshot.sh    # deploy to https://nexus.vtl.name.vn/repository/maven-snapshots/
./release.sh     # deploy to https://nexus.vtl.name.vn/repository/maven-releases/
```

PowerShell equivalents (`snapshot.ps1`, `release.ps1`) are available for Windows. All scripts run Maven with `-DskipTests=true` and UTF-8 encoding by default.

## Documentation

For in-depth documentation, see the `docs/` directory:

| Document                                                      | What it covers                                                                                |
| ------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| [docs/project-overview.md](docs/project-overview.md)             | Architecture, technology stack, all modules, configuration                                    |
| [docs/classpool-di-container.md](docs/classpool-di-container.md) | `ClassPool` DI container, `@Component`, `@Instance`, `@Qualifier`, lifecycle          |
| [docs/vertx-web-framework.md](docs/vertx-web-framework.md)       | HTTP routing,`@Controller`/`@RestController`, request binding, validation, middleware     |
| [docs/database-layer.md](docs/database-layer.md)                 | JDBC, entity annotations, DDL generation, JPA repositories, reactive repositories, pagination |
| [docs/annotation-processors.md](docs/annotation-processors.md)   | `@MapperClass` processor, `@Mapping`/`@Merging`, all 9 processor cross-references       |
| [docs/utilities.md](docs/utilities.md)                           | Utilities, shared infrastructure, constants, custom exceptions, package scanner               |

## Troubleshooting

- Verify you are building with JDK 17; the Maven configuration targets Java 17 bytecode.
- If deployments fail, confirm server IDs and credentials in `~/.m2/settings.xml` match the Nexus endpoints above.
- On Windows, set `DEV_KIT_LOCATION` before running the PowerShell scripts, or invoke Maven directly.
- Add the appropriate JDBC driver dependencies to your application; they are not shaded into the library.
