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

---

## Quickstart

Build all modules:

```bash
mvn clean install -DskipTests=true
```

Build a single module (example):

```bash
mvn -pl common-lib -am clean install -DskipTests=true
```

---

## Helper scripts and required environment

Scripts are provided for both Bash and PowerShell.

- Linux/macOS: `build.sh`, `clean.sh`, `snapshot.sh`, `release.sh`
- Windows PowerShell: `build.ps1`, `clean.ps1`, `snapshot.ps1`, `release.ps1`

The scripts expect JDK and Maven locations via environment variables:

- Bash scripts set `JAVA_HOME` to `$HOME/dev-kit/jdk-11` and `MAVEN_HOME` to `$HOME/dev-kit/maven`.
- PowerShell scripts expect `DEV_KIT_LOCATION` to be set so that:
  - `JAVA_HOME = "$env:DEV_KIT_LOCATION\jdk-11"`
  - `MAVEN_HOME = "$env:DEV_KIT_LOCATION\maven"`

If your tools are installed elsewhere, either export the matching variables or run Maven directly without the scripts.

---

## Publishing (Snapshots/Releases)

Use the provided scripts to deploy to your Nexus repositories (credentials must be configured in your Maven settings):

```bash
# Snapshot
./snapshot.sh          # Bash
./snapshot.ps1         # PowerShell

# Release
./release.sh           # Bash
./release.ps1          # PowerShell
```

Configured endpoints (from scripts):

- Snapshots: `https://nexus.vtl.name.vn/repository/maven-snapshots/`
- Releases:  `https://nexus.vtl.name.vn/repository/maven-releases/`

Maven runs with `-DskipTests=true` and UTF-8 file encoding by default in these scripts.

---

## Using the annotation processor

Add the processor on the compile classpath (provided scope is typical):

```xml
<dependency>
  <groupId>vn.com.lcx</groupId>
  <artifactId>processor</artifactId>
  <version>3.4.3.lcx-SNAPSHOT</version>
  <scope>provided</scope>
  <!-- or annotationProcessor path if you prefer -->
</dependency>
```

If your build does not automatically pick up the processor, configure the Maven Compiler Plugin explicitly:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>vn.com.lcx</groupId>
        <artifactId>processor</artifactId>
        <version>3.4.3.lcx-SNAPSHOT</version>
      </path>
    </annotationProcessorPaths>
    <compilerArgs>
      <arg>-proc:full</arg>
    </compilerArgs>
  </configuration>
  <!-- lock version via your parent/BOM as needed -->
  <version>3.14.1</version>
  <!-- version shown here for clarity; align with your build -->
</plugin>
```

Generated sources will be produced during compilation based on annotations defined in `common-lib` and templates under `common-lib/src/main/resources/template/`.

---

## Troubleshooting

- Ensure you are on JDK 11 when building this project (the POMs set the compiler target to 11).
- If Maven cannot deploy, verify credentials and server IDs in your `~/.m2/settings.xml` match the repository endpoints above.
- On Windows, set `DEV_KIT_LOCATION` before using the PowerShell scripts, or invoke Maven directly.
