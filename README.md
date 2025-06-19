# jlcx-lib

**jlcx-lib** is a lightweight, reusable Java library designed to accelerate backend development by reducing boilerplate code and providing robust, easy-to-use building blocks.

---

## ✨ Features

- **Database Connection Utilities**: Simple connection pool management and configuration.
- **Lightweight ORM**: Automatic mapping of Java entities to database tables.
- **SQL Generator**: Generates CRUD (Create, Read, Update, Delete) SQL queries at compile time based on entity definitions.
- **Annotation Processor**: Enables source code generation during compilation using custom annotations.
- **Vert.x Integration**: Support for reactive programming with Vert.x.
- **gRPC Proto Plugin**: Automatically generates gRPC code from `.proto` definitions.
- **Dependency Injection**: Annotation-based injection for modularity, inspired by Spring Boot.
- **Common Utilities**: Includes reusable utility classes for properties, YAML, encryption, and more.

---

## 📦 Module Structure

```
jlcx-lib/
├── common-lib/           # Shared utilities and base classes
├── processor/            # Annotation processor for code generation
├── grpc-proto-plugin/    # Plugin for processing gRPC proto files
└── example/              # Example usage of the library
```

---

## 🚀 Getting Started

### 1. Add the parent to your `pom.xml`:

```xml
<parent>
    <groupId>vn.com.lcx</groupId>
    <artifactId>lcx-lib</artifactId>
    <version>2.0</version>
    <relativePath/>
</parent>
```

### 2. Add required dependencies:

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

### 3. Define an entity using annotations

Example:
```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@SQLMapping
@TableName("users")
public class User {
    @IdColumn
    @ColumnName("id")
    private Long id;
    @ColumnName("username")
    private String username;
    // ...
}
```
The library will automatically generate SQL queries and helper classes during compilation.

See the `example/` directory for a complete demo on configuration, entity creation, and usage.

---

## 🛠 Build & Run

You can build the project using the provided shell script or Maven:

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
> **Note:** The build scripts assume `jdk-11` and `maven` are available in `$HOME/dev-kit`.

---

## ✅ Requirements

- Java 11 or higher
- Maven 3.6+

---

## 🧪 Testing

Unit tests are provided for core utility classes.  
To run all tests:

```bash
mvn test
```

Example tested classes:
- `LCXProperties`
- `YamlProperties`

You can find the test sources in `common-lib/src/test/java/vn/com/lcx/common/utils/`.

---

## 🤝 Contributing

Contributions are welcome!  
You can:
- Open an issue for bugs or feature requests.
- Fork the repository and submit a pull request.

---

## 📄 License

This project is licensed under the MIT License.

---
