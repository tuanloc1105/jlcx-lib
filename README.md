# jlcx-lib

**jlcx-lib** is a lightweight and reusable Java library that provides utilities for database access, SQL generation, annotation processing, Vert.x integration, and gRPC support.
It is designed to speed up backend development by minimizing boilerplate code and offering easy-to-use building blocks.

---

## Features

* **Database Connection Utilities** – Simple connection pool management and configuration.
* **Lightweight ORM** – Automatically maps Java entities to database tables.
* **SQL Generator** – Generate SQL queries (CRUD) at compile time based on entity definitions.
* **Annotation Processor** – Generate source code during compilation using custom annotations.
* **Vert.x Integration** – Support for reactive programming with Vert.x.
* **gRPC Proto Plugin** – Automatically generate gRPC code from `.proto` definitions.
* **Dependency Injection** – Inspired by Spring Boot, supports annotation-based injection for modular.

---

## 📦 Module Structure

```txt
jlcx-lib/
├── common-lib/           # Shared utilities and base classes
├── processor/            # Annotation processor for code generation
├── vertx-processor/      # Vert.x-specific annotation processing
├── grpc-proto-plugin/    # Plugin for processing gRPC proto files
└── example/              # Example usage of the library
```

---

## 🚀 Getting Started

To start using **jlcx-lib** in your Maven project, follow the steps below.

### 1. Add the parent to your `pom.xml`:

```xml
<parent>
  <groupId>vn.com.lcx</groupId>
  <artifactId>lcx-lib</artifactId>
  <version>1.0</version>
  <relativePath/>
</parent>
```

---

### 2. Add the required dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>vn.com.lcx</groupId>
    <artifactId>processor</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>vn.com.lcx</groupId>
    <artifactId>vertx-processor</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>vn.com.lcx</groupId>
    <artifactId>base-vertx</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### 3. Define an entity using annotations:

```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@SQLMapping
@TableName(value = "user", schema = "lcx")
public class User implements Serializable {
    private static final long serialVersionUID = 2675938794277420417L;

    @IdColumn
    @ColumnName(name = "id")
    private Long id;

    @ColumnName(name = "username", nullable = false)
    private String username;

    @ColumnName(name = "password", nullable = false)
    private String password;

    @ColumnName(name = "full_name")
    private String fullName;

    @ColumnName(name = "active", defaultValue = "false")
    private Boolean active;

    @ColumnName(name = "created_time", defaultValue = "current_timestamp")
    private LocalDateTime createdTime;

    @ColumnName(name = "updated_time", defaultValue = "current_timestamp")
    private LocalDateTime updatedTime;

}
```

The library will automatically generate SQL queries and necessary helper classes during compilation.

👉 **Refer to the [`example/`](./example)** directory for a complete demo on configuration, entity creation, and usage examples.

---

## 🛠 Build & Run

Use the provided shell script or Maven:

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

---

## ✅ Requirements

* Java 11 or higher
* Maven 3.6+

---

## 🤝 Contributing

We welcome contributions from the community!

* Open an issue for bugs or feature requests
* Fork the repo and submit a pull request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---
