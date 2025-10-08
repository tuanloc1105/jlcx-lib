## jlcx-lib

Utilities, annotations, and an annotation processor to speed up Java backend development on Vert.x. The library provides database helpers, reactive/Vert.x base classes, simple JPA helpers, code-generation templates, and an annotation processor that scans and generates code at compile time.

### Modules

- `common-lib`: Core utilities and features (annotations, JDBC helpers, Vert.x base, reactive helpers, simple JPA helpers, templates, default logging resources).
- `processor`: Annotation Processor registered via `META-INF/services/javax.annotation.processing.Processor` that works with the annotations and templates from `common-lib`.
- `examples`: Working samples demonstrating usage: gRPC (server/client), Hibernate Reactive, and a Todo app.

### Tech stack

- Java 11, Maven
- Vert.x 5.x (core, web, auth-jwt, grpc, micrometer, redis, DB clients)
- Databases: Oracle, PostgreSQL, MySQL, SQL Server, H2; pool: HikariCP
- Serialization: Jackson, Gson; YAML: SnakeYAML
- Logging: SLF4J + Logback
- Testing: JUnit Jupiter, Mockito, Datafaker

---

## Project layout

```
jlcx-lib/
├─ common-lib/
│  ├─ src/main/java/vn/com/lcx/common/
│  │  ├─ annotation/            # SQLMapping, TableName, ColumnName, IdColumn, Component, Controller, ...
│  │  ├─ database/              # JDBC utilities, mappers, pooling helpers, etc.
│  │  ├─ vertx/base/            # Vert.x base classes
│  │  ├─ reactive/              # Reactive helpers
│  │  └─ jpa/                   # JPA helpers
│  └─ src/main/resources/
│     ├─ default-logback.xml
│     ├─ default-banner.txt
│     └─ template/              # codegen templates (repository, service, controller, sql-mapping, ...)
├─ processor/
│  └─ src/main/resources/META-INF/services/javax.annotation.processing.Processor
└─ examples/
   ├─ grpc-example/
   ├─ hibernate-reactive-example/
   └─ todo-app-example/
```

---

## Requirements

- JDK 11+
- Maven 3.6+
- (Optional) Node.js for the web assets under `examples/todo-app-example/web`

---

## Using in your Maven project

Add dependencies (adjust version as needed):

```xml
<dependency>
  <groupId>vn.com.lcx</groupId>
  <artifactId>common-lib</artifactId>
  <version>3.4.3.lcx-SNAPSHOT</version>
</dependency>

<!-- Annotation processor (compile-time) -->
<dependency>
  <groupId>vn.com.lcx</groupId>
  <artifactId>processor</artifactId>
  <version>3.4.3.lcx-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

If you manage versions via your own BOM/parent, add the coordinates under `dependencyManagement` and reference without the `<version>` tag.

---

## Build

From repository root:

```bash
mvn clean install
```

Or use helper scripts:

```bash
# Linux/macOS
./build.sh
./clean.sh

# Windows PowerShell
./build.ps1
./clean.ps1
```

Snapshot/Release helper scripts are available as well (`snapshot.sh/.ps1`, `release.sh/.ps1`); integrate with your internal repository as appropriate.

---

## Examples

- `examples/grpc-example`: Vert.x gRPC server and client. See its `README.md` for run commands.
- `examples/hibernate-reactive-example`: Hibernate Reactive + Vert.x demo (includes `application.yaml`, `logback.xml`, and `persistence.xml`).
- `examples/todo-app-example`: Todo backend on Vert.x using `common-lib` and `processor` (packaged with Spring Boot repackage plugin for fat jar).

Build an example (per subfolder):

```bash
cd examples/todo-app-example
mvn clean package
```

Each example also includes `build.sh`, `clean.sh`, and PowerShell equivalents.

---

## Configuration notes

- Default logging config: `common-lib/src/main/resources/default-logback.xml`.
- Default banner: `common-lib/src/main/resources/default-banner.txt`.
- Code-generation templates: `common-lib/src/main/resources/template/`.
- Database drivers are not shaded automatically; add the appropriate driver(s) to your application POM.

---

## Versioning

The current version in the root POM is `3.4.3.lcx-SNAPSHOT`.
