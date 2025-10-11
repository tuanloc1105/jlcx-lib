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

- Java 11, Maven 3.6+
- Vert.x 5.x (core, web, auth-jwt, grpc, micrometer, redis, database clients)
- Databases: Oracle, PostgreSQL, MySQL, SQL Server, H2 (via HikariCP)
- Serialization: Jackson, Gson; YAML: SnakeYAML
- Logging: SLF4J + Logback
- Testing: JUnit Jupiter, Mockito, Datafaker

## Prerequisites

- JDK 11 or newer
- Maven 3.6 or newer
- (Optional) Node.js for the web assets in `examples/todo-app-example/web`

## Quick start

Clone the repository and build all modules:

```bash
mvn clean install
```

Helper scripts are available if you prefer: `./build.sh`, `./clean.sh` (or the PowerShell equivalents on Windows). Scripts assume `JAVA_HOME` and `MAVEN_HOME` under `$HOME/dev-kit` on Bash, or `DEV_KIT_LOCATION` on PowerShell; override those variables if your tooling lives elsewhere.

## Use in your Maven project

Add the core library and annotation processor to your project (adjust the version as needed):

```xml
<dependency>
  <groupId>vn.com.lcx</groupId>
  <artifactId>common-lib</artifactId>
  <version>3.4.3.lcx</version>
</dependency>

<!-- Annotation processor (compile-time) -->
<dependency>
  <groupId>vn.com.lcx</groupId>
  <artifactId>processor</artifactId>
  <version>3.4.3.lcx</version>
  <scope>provided</scope>
</dependency>
```

If you manage versions centrally, declare the coordinates in your BOM or parent POM and omit the `<version>` tags above.

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
        <groupId>vn.com.lcx</groupId>
        <artifactId>processor</artifactId>
        <version>3.4.3.lcx</version>
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
