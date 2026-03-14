# jlcx-lib

jlcx-lib is an opinionated toolkit for building reactive Java services on Vert.x. It bundles utility classes, database helpers, code-generation templates, and an annotation processor so teams can ship production-ready microservices without re-implementing infrastructure pieces. The repository contains the library itself plus runnable examples that show common backend patterns.

## Highlights

- Core utilities for JDBC, Vert.x base classes, reactive helpers, and lightweight JPA support.
- Annotation-driven code generation that scans your project at compile time and produces repositories, services, controllers, and SQL mappings.
- Ready-to-use templates, logging defaults, and banner assets that keep your services consistent from the first commit.
- End-to-end examples (gRPC, Hibernate Reactive, Todo app) that double as documentation and integration tests for the toolkit.

## Modules

- `common-lib`: Shared annotations, database utilities, Vert.x base components, reactive helpers, JPA helpers, templates, and default logging resources.
- `processor`: Annotation processor registered via `META-INF/services/javax.annotation.processing.Processor` that works with the annotations and templates from `common-lib`.
- `examples`: Runnable Vert.x projects that demonstrate typical usage patterns.

## Tech stack

- Java 17, Maven 3.9+
- Vert.x 5.x (core, web, auth-jwt, grpc, micrometer, redis, database clients)
- Databases: Oracle, PostgreSQL, MySQL, SQL Server, H2 (via HikariCP)
- Serialization: Jackson, Gson; YAML: SnakeYAML
- Logging: SLF4J + Logback
- Testing: JUnit Jupiter, Mockito, Datafaker

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- (Optional) Node.js for the web assets in `examples/todo-app-example/web`

## Installation

To use `jlcx-lib`, you need to pull the source code and build it locally.

### 1. Clone the repository

```bash
git clone https://github.com/tuanloc1105/jlcx-lib.git
cd jlcx-lib
```

### 2. Build and Install

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
  <version>4.0.3.lcx</version>
</dependency>

<!-- Annotation processor (compile-time) -->
<dependency>
  <groupId>vn.io.lcx</groupId>
  <artifactId>processor</artifactId>
  <version>4.0.3.lcx</version>
  <scope>provided</scope>
</dependency>
```

If you manage versions centrally, declare the coordinates in your BOM or parent POM and omit the `<version>` tags above.


## Configuration Reference

The library uses a set of configuration keys (typically in `application.yaml`) to set up database connections, metrics, and server options. Below is the reference for the supported properties.

### General Configuration

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.port` | Integer | The HTTP server port for the application (default: `8080`). Used in the generated application verticle. |
| `server.enable-http-2` | Boolean | Enable HTTP/2 support for the server (default: `false`). Used in the generated application verticle. |

### JDBC Database Configuration (Hibernate)

Used when configuring Hibernate `SessionFactory`.

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.database.host` | String | Database host address. |
| `server.database.port` | Integer | Database port. |
| `server.database.username` | String | Database username. |
| `server.database.password` | String | Database password. |
| `server.database.name` | String | Database name. |
| `server.database.schema_name` | String | (Optional) Default schema name. |
| `server.database.type` | Enum | Database type. Supported values: `ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`. Determines default driver and connection string. |
| `server.database.driver_class_name` | String | JDBC driver class name. Defaults to the one associated with `server.database.type` if not specified. |
| `server.database.initial_pool_size` | Integer | Initial size of the connection pool (HikariCP `minimumIdle`). |
| `server.database.max_pool_size` | Integer | Maximum size of the connection pool (HikariCP `maximumPoolSize`). |
| `server.database.max_timeout` | Integer | Connection timeout in seconds (multiplied by 1000 for HikariCP `connectionTimeout`). |
| `server.database.dialect` | String | **(Deprecated/Unused)** Hibernate dialect class name. The library typically relies on Hibernate or the `server.database.type` to determine the dialect. |

### Reactive Database Configuration (Vert.x SQL Clients)

Used by `ReactiveDbClientConfiguration` to create Vert.x SQL pools.

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.reactive.database.host` | String | Reactive database host. |
| `server.reactive.database.port` | Integer | Reactive database port. |
| `server.reactive.database.username` | String | Reactive database username. |
| `server.reactive.database.password` | String | Reactive database password. |
| `server.reactive.database.name` | String | Reactive database name. |
| `server.reactive.database.max_pool_size` | Integer | Maximum pool size for the reactive client. |
| `server.reactive.database.type` | Enum | Database type. Supported values: `ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`. |

### Hibernate Reactive Configuration

Used when configuring Hibernate Reactive `Stage.SessionFactory`.

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.hreactive.database.host` | String | Database host address. |
| `server.hreactive.database.port` | Integer | Database port. |
| `server.hreactive.database.username` | String | Database username. |
| `server.hreactive.database.password` | String | Database password. |
| `server.hreactive.database.name` | String | Database name. |
| `server.hreactive.database.type` | Enum | Database type. Supported values: `ORACLE`, `POSTGRESQL`, `MYSQL`, `MSSQL`. |
| `server.hreactive.database.max_pool_size` | Integer | Maximum size of the connection pool. |

### Redis Configuration (Vert.x Redis)

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.reactive.redis.host` | String | Redis host. |
| `server.reactive.redis.port` | Integer | Redis port. |
| `server.reactive.redis.password` | String | Redis password. |
| `server.reactive.database.max_pool_size` | Integer | **Note:** The Redis client configuration currently reuses `server.reactive.database.max_pool_size` for its pool size (default `5` if not set). |

### Metrics Configuration

| Key | Type | Description |
| :--- | :--- | :--- |
| `server.metrics.enable` | Boolean | Enables Vert.x Micrometer metrics (Prometheus). If true, it may start a separate embedded metrics server based on `server.metrics.port`. |
| `server.metrics.port` | Integer | Port for the embedded metrics server (default `8081`). |
| `server.metrics.endpoint` | String | Endpoint for the embedded metrics server (default `/metrics`). |
| `server.enable-metrics` | Boolean | Enables the `/metrics` route handler in the **main** application router (as defined in `ApplicationVerticle` template). |

### JSON Configuration

| Key | Type | Description |
| :--- | :--- | :--- |
| `json.sensitive_field` | List/Array | A list of field names that should be masked/obfuscated when serializing JSON (e.g. logging). |

## Running the examples

Each example module can be built independently:

```bash
cd examples/todo-app-example
mvn clean package
```

- `examples/grpc-example`: Vert.x gRPC server and client (see its `README.md` for run commands).
- `examples/hibernate-reactive-example`: Hibernate Reactive + Vert.x demo with `application.yaml`, `logback.xml`, and `persistence.xml`.
- `examples/todo-app-example`: Todo backend built with `common-lib` and `processor`, packaged with the Spring Boot repackage plugin for an executable JAR.

Each example includes `build.sh`, `clean.sh`, and PowerShell counterparts.

## Annotation processor

The annotation processor runs during compilation and generates sources based on the annotations and templates in `common-lib`. If your build does not automatically pick it up, wire it into the Maven Compiler Plugin:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.14.1</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>vn.io.lcx</groupId>
        <artifactId>processor</artifactId>
        <version>4.0.3.lcx</version>
      </path>
    </annotationProcessorPaths>
    <compilerArgs>
      <arg>-proc:full</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

Generated sources are emitted under Maven's default `target/generated-sources/annotations` directory.

## Publishing

Use the provided scripts to push artifacts to Nexus (credentials must be configured in your `~/.m2/settings.xml`):

```bash
./snapshot.sh    # deploy to https://nexus.vtl.name.vn/repository/maven-snapshots/
./release.sh     # deploy to https://nexus.vtl.name.vn/repository/maven-releases/
```

PowerShell equivalents (`snapshot.ps1`, `release.ps1`) are available for Windows. All scripts run Maven with `-DskipTests=true` and UTF-8 encoding by default.

## Repository layout

```
jlcx-lib/
├─ common-lib/
│  ├─ src/main/java/vn/com/lcx/common/
│  └─ src/main/resources/
├─ processor/
│  └─ src/main/resources/META-INF/services/javax.annotation.processing.Processor
└─ examples/
   ├─ grpc-example/
   ├─ hibernate-reactive-example/
   └─ todo-app-example/
```

## Troubleshooting

- Verify you are building with JDK 11; the Maven configuration targets Java 11 bytecode.
- If deployments fail, confirm server IDs and credentials in `~/.m2/settings.xml` match the Nexus endpoints above.
- On Windows, set `DEV_KIT_LOCATION` before running the PowerShell scripts, or invoke Maven directly.
- Add the appropriate JDBC driver dependencies to your application; they are not shaded into the library.
