# jlcx-lib

**jlcx-lib** is a lightweight and reusable Java library designed to accelerate backend development by reducing boilerplate code and providing easy-to-use building blocks.

## Features

* **Database Connection Utilities**: Offers simple connection pool management and configuration.
* **Lightweight ORM**: Automatically maps Java entities to database tables.
* **SQL Generator**: Generates CRUD (Create, Read, Update, Delete) SQL queries at compile time based on entity definitions.
* **Annotation Processor**: Enables source code generation during compilation using custom annotations.
* **Vert.x Integration**: Provides support for reactive programming with Vert.x.
* **gRPC Proto Plugin**: Automatically generates gRPC code from `.proto` definitions.
* **Dependency Injection**: Features annotation-based injection for modularity, inspired by Spring Boot.

## 📦 Module Structure

The project is organized into the following modules:
```txt
jlcx-lib/
├── common-lib/           # Shared utilities and base classes
├── processor/            # Annotation processor for code generation
├── vertx-processor/      # Vert.x-specific annotation processing (Note: The provided files do not include this module explicitly in the directory structure, but it's mentioned in the dependency section of the example pom.xml and common-lib pom.xml with version 1.0.0 and in common-lib/src/main/java/vn/com/lcx/vertx, common-lib/src/main/java/vn/com/lcx/vertx/base)
├── grpc-proto-plugin/    # Plugin for processing gRPC proto files
└── example/              # Example usage of the library
```

## 🚀 Getting Started

To use **jlcx-lib** in a Maven project:

### 1. Add the parent to your `pom.xml`:

```xml
<parent>
    <groupId>vn.com.lcx</groupId>
    <artifactId>lcx-lib</artifactId>
    <version>2.0</version>
    <relativePath/>
</parent>
```
*(Note: The `pom.xml` in the root and `common-lib/pom.xml` indicates version 2.0 and 3.0.0 for `lcx-lib` and `common-lib` respectively, while the README shows 1.0. This might be a versioning discrepancy.)*

### 2. Add the required dependencies:

```xml
<dependency>
    <groupId>vn.com.lcx</groupId>
    <artifactId>processor</artifactId>
    <version>3.0.0</version>
</dependency>

<dependency>
    <groupId>vn.com.lcx</groupId>
    <artifactId>common-lib</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 3. Define an entity using annotations:

An example of an entity definition with annotations like `@AllArgsConstructor`, `@NoArgsConstructor`, `@Data`, `@SQLMapping`, `@TableName`, `@IdColumn`, and `@ColumnName` is provided. The library automatically generates SQL queries and helper classes during compilation.

A complete demo on configuration, entity creation, and usage examples can be found in the `example/` directory.

## 🛠 Build & Run

The project can be built using the provided shell script or Maven:

```bash
# Build the entire project
./build.sh

# Or using Maven
mvn clean install

# Run example
cd example
./build-web.sh
./build.sh
java -jar target/todo-app-example-1.0.0-jar-with-dependencies.jar
```
*(Note: The build scripts refer to `jdk-11` and `maven` being located in `$HOME/dev-kit` which would need to be set up by the user running the build.)*

## ✅ Requirements

* Java 11 or higher
* Maven 3.6+

## 🤝 Contributing

Contributions are welcome. Users can:
* Open an issue for bugs or feature requests.
* Fork the repository and submit a pull request.

## 📄 License

This project is licensed under the MIT License.
