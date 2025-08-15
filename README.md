[![Quality gate](https://sonar.vtl.name.vn/api/project_badges/quality_gate?project=jlcx-lib&token=sqb_c013aa1bd67625327e8e168b4891fc2eaba2dd66)](https://sonar.vtl.name.vn/dashboard?id=jlcx-lib)

# jlcx-lib

**jlcx-lib** is a comprehensive Java development framework designed to accelerate backend application development by providing powerful, lightweight, and easy-to-use building blocks. It combines the best practices of modern Java development with innovative code generation techniques to reduce boilerplate code and improve developer productivity.

---

## ✨ Key Features

### 🗄️ **Database & ORM**
- **Multi-Database Support**: Native support for PostgreSQL, MySQL, Oracle, and SQL Server
- **Lightweight ORM**: Automatic entity-to-table mapping with custom annotations
- **SQL Code Generation**: Compile-time generation of CRUD operations and SQL queries
- **Connection Pool Management**: Built-in HikariCP integration with optimized configurations
- **Database Schema Generation**: Automatic DDL generation from entity definitions

### 🔄 **Reactive Programming**
- **Vert.x Integration**: Full support for reactive programming with Vert.x 5.x
- **Reactive Repositories**: Asynchronous database operations with Future-based APIs
- **Reactive Controllers**: Non-blocking HTTP request handling
- **WebSocket Support**: Built-in WebSocket handling capabilities
- **Event-Driven Architecture**: Native support for event-driven application patterns

### 🏗️ **Code Generation & Annotations**
- **Annotation Processors**: Compile-time code generation for repositories, services, and controllers
- **SQL Mapping**: Automatic generation of SQL utilities and mapping classes
- **Custom Annotations**: Rich set of annotations for dependency injection and configuration

### 🔧 **Dependency Injection & Configuration**
- **Lightweight DI Container**: Annotation-based dependency injection inspired by Spring Boot
- **Component Scanning**: Automatic discovery and registration of components
- **Configuration Management**: Flexible configuration system with environment variable support
- **Property Management**: Utilities for handling properties, YAML, and configuration files

### 🛡️ **Security & Authentication**
- **JWT Authentication**: Built-in JWT token generation and validation
- **API Key Support**: Configurable API key authentication
- **Password Encryption**: BCrypt integration for secure password hashing
- **Authorization Context**: Request-scoped authentication context management

### 📊 **Monitoring & Observability**
- **Metrics Integration**: Micrometer and Prometheus metrics support
- **Health Checks**: Built-in health check endpoints
- **Logging**: Structured logging with Logback integration
- **Tracing**: Request tracing and correlation ID support

### 🧪 **Testing & Development**
- **Unit Testing**: Comprehensive test utilities and mocking support
- **Integration Testing**: Vert.x test framework integration
- **Data Faker**: Test data generation utilities
- **Development Tools**: Hot reload and development-friendly configurations

---

## 📦 Module Structure

```
jlcx-lib/
├── common-lib/        # Core utilities, database tools, and shared components
│   ├── annotation/    # Custom annotations for code generation
│   ├── database/      # Database utilities and connection management
│   ├── utils/         # Common utility classes
│   ├── vertx/         # Vert.x integration components
│   ├── reactive/      # Reactive programming support
│   └── jpa/           # JPA/Hibernate integration
├── processor/         # Annotation processors for code generation
└── example/           # Complete example application
    ├── src/main/java/ # Backend example code
    └── web/           # Frontend React application
```

---

## 🚀 Quick Start

### 1. **Add Dependencies**

Add the parent POM to your project:

```xml
<parent>
    <groupId>vn.com.lcx</groupId>
    <artifactId>lcx-lib</artifactId>
    <version>3.1.3.lcx-beta</version>
    <relativePath/>
</parent>
```

Add required dependencies:

```xml
<dependencies>
    <!-- Core library -->
    <dependency>
        <groupId>vn.com.lcx</groupId>
        <artifactId>common-lib</artifactId>
        <version>3.1.3.lcx-beta</version>
    </dependency>

    <!-- Annotation processor for code generation -->
    <dependency>
        <groupId>vn.com.lcx</groupId>
        <artifactId>processor</artifactId>
        <version>3.1.3.lcx-beta</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<repositories>

    <repository>
        <id>nexus-releases</id>
        <url>https://nexus.vtl.name.vn/repository/maven-releases/</url>
    </repository>

    <repository>
        <id>nexus-snapshots</id>
        <url>https://nexus.vtl.name.vn/repository/maven-snapshots/</url>
    </repository>

</repositories>

```

### 2. **Define Your Entity**

```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@SQLMapping
@TableName("users")
@Entity
public class User {
    @IdColumn
    @ColumnName("id")
    private Long id;
    
    @ColumnName("username")
    private String username;
    
    @ColumnName("email")
    private String email;
    
    @ColumnName("created_at")
    private LocalDateTime createdAt;
}
```

### 3. **Create Repository Interface**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods will be automatically implemented
    List<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
```

### 4. **Create Service Layer**

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    @Transactional
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }
    
    public List<User> getAllUsers() {
        return userRepository.find(null);
    }
}
```

### 5. **Create Reactive Controller**

```java
@Component
@Controller(path = "/api/users")
public class UserController extends ReactiveController {
    
    private final UserService userService;
    
    @Post(path = "/create")
    public void createUser(RoutingContext ctx) {
        CreateUserRequest request = handleRequest(ctx, gson, CreateUserRequest.class);
        userService.createUser(request.toUser())
            .onSuccess(user -> handleResponse(ctx, gson, user))
            .onFailure(err -> handleError(ctx, gson, err));
    }
}
```

### 6. **Configure Application**

```java
@VertxApplication(staticResource = true)
public class App {
    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(App.class);
    }
}
```

---

## 🛠️ Build & Run

### **Prerequisites**
- Java 11 or higher
- Maven 3.6+
- Node.js 16+ (for frontend development)

### **Build Commands**

```bash
# Build the entire project
./build.sh

# Or using Maven
mvn clean install

# Build and run the example application
cd example
./build-web.sh    # Build frontend
./build.sh        # Build backend
java -jar target/todo-app-example-1.0.0-jar-with-dependencies.jar
```

### **Development Scripts**

```bash
# Clean build artifacts
./clean.sh

# Windows PowerShell
./clean.ps1

# Build with custom configuration
mvn clean install -DskipTests=true
```

---

## 🔧 Configuration

### **Database Configuration**

```yaml
# application.yaml
database:
  host: localhost
  port: 5432
  name: myapp
  username: postgres
  password: password
  type: POSTGRESQL
  max-pool-size: 20
  initial-pool-size: 5
```

### **Server Configuration**

```yaml
server:
  port: 8080
  enable-metrics: true
  api-key: your-api-key-here
  jwt-secret: your-jwt-secret
```

### **Logging Configuration**

```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

## 📚 Advanced Features

### **Custom Annotations**

```java
// SQL Mapping for custom queries
@SQLMapping
@TableName("custom_table")
public class CustomEntity {
    // Automatically generates SQL utilities
}

// Mapper for object transformation
@MapperClass
public interface UserMapper {
    @Mapping(source = "user", target = "userDto")
    UserDTO toDTO(User user);
}

// Reactive repository
@RRepository
public interface ReactiveUserRepository extends ReactiveRepository<User> {
    Future<List<User>> findByStatus(RoutingContext ctx, SqlConnection conn, String status);
}
```

### **Database Schema Generation**

```java
// Automatically generates DDL from entity definitions
EntityAnalyzer analyzer = new EntityAnalyzer(
    new EntityAnalysisContext(User.class, "POSTGRESQL", "sql/")
);
analyzer.analyze(); // Generates create-table.sql
```

---

## 🧪 Testing

### **Unit Tests**

```java
@Test
public void testUserCreation() {
    User user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    
    User savedUser = userService.createUser(user);
    
    assertNotNull(savedUser.getId());
    assertEquals("testuser", savedUser.getUsername());
}
```

### **Integration Tests**

```java
@VertxTest
public class UserControllerTest {
    
    @Test
    public void testCreateUser(Vertx vertx, VertxTestContext testContext) {
        // Test reactive endpoints
        WebClient client = WebClient.create(vertx);
        
        client.post(8080, "localhost", "/api/users/create")
            .sendJson(new CreateUserRequest("testuser", "test@example.com"))
            .onSuccess(response -> {
                assertEquals(200, response.statusCode());
                testContext.completeNow();
            })
            .onFailure(testContext::failNow);
    }
}
```

---

## 📖 API Documentation

### **Core Annotations**

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@SQLMapping` | Generate SQL utilities | `@SQLMapping class User` |
| `@TableName` | Specify database table | `@TableName("users")` |
| `@ColumnName` | Map field to column | `@ColumnName("user_name")` |
| `@IdColumn` | Mark primary key | `@IdColumn private Long id` |
| `@Repository` | Generate repository impl | `@Repository interface UserRepo` |
| `@Service` | Generate service proxy | `@Service class UserService` |
| `@Component` | Register for DI | `@Component class MyService` |
| `@Controller` | Define REST endpoint | `@Controller(path="/api")` |

### **Database Operations**

```java
// Basic CRUD
userRepository.save(user);
userRepository.update(user);
userRepository.delete(user);
Optional<User> user = userRepository.findById(1L);

// Custom queries
List<User> users = userRepository.find(criteria -> 
    criteria.equal("status", "ACTIVE")
);

// Pagination
Page<User> page = userRepository.find(
    criteria -> criteria.like("name", "%john%"),
    Pageable.of(0, 10, Sort.by("name"))
);
```

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### **Development Setup**

```bash
# Clone the repository
git clone https://github.com/your-username/jlcx-lib.git
cd jlcx-lib

# Install dependencies
mvn clean install

# Run tests
mvn test

# Build example
cd example
mvn clean package
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🆘 Support

- **Documentation**: Check the `example/` directory for complete usage examples
- **Issues**: Report bugs and request features via GitHub Issues
- **Discussions**: Join community discussions for questions and ideas

---

## 🔄 Version History

- **v2.0**: Major release with Vert.x 5.x support, enhanced reactive programming, and improved code generation
- **v1.0**: Initial release with basic ORM and annotation processing capabilities

---

**Built with ❤️ for the Java community**
