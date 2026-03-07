# ClassPool - Lightweight DI Container

## Overview

`ClassPool` is a reflection-based dependency injection container located at
`common-lib/src/main/java/vn/com/lcx/common/config/ClassPool.java`.

It handles:
- **Package scanning** - discovers classes via `PackageScanner`
- **Bean lifecycle** - instantiation, dependency resolution, registration
- **Factory methods** - `@Instance` methods with parameter injection
- **Conditional creation** - `@DependsOn` for prerequisite beans
- **Vert.x integration** - verticle discovery and deployment

All annotations are in package `vn.io.lcx.common.annotation`.

---

## Annotations Reference

### @Component

Marks a class as a managed bean.

| Attribute | Target | Retention |
|-----------|--------|-----------|
| *(none)*  | `TYPE`   | `RUNTIME`   |

**Constraints:**
- Must have exactly **1 constructor**
- Simple component (no non-static fields) -> instantiated via no-arg constructor
- Dependent component (has non-static fields) -> instantiated via constructor injection

```java
@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

---

### @Instance

Marks a method inside a `@Component` as a factory method. The return value is registered as a bean.

| Attribute | Type     | Default | Description                        |
|-----------|----------|---------|------------------------------------|
| `value`   | `String` | `""`    | Custom bean name (method name if blank) |

| Target   | Retention |
|----------|-----------|
| `METHOD` | `RUNTIME`   |

**Constraints:**
- Return type must be **non-void**
- Parameters are resolved from the pool (supports `@Qualifier`)
- If parameters can't be resolved immediately, invocation is **deferred**

```java
@Component
public class AppConfig {

    // No-arg factory
    @Instance
    public Gson gson() {
        return new GsonBuilder().create();
    }

    // Named instance
    @Instance("primaryDataSource")
    public DataSource primaryDs() {
        return DataSourceBuilder.create().url("jdbc:...").build();
    }

    // With bean parameter
    @Instance
    public UserService userService(UserRepository repo) {
        return new UserService(repo);
    }

    // With @Qualifier parameter
    @Instance
    public ReportService reportService(@Qualifier("secondaryDataSource") DataSource ds) {
        return new ReportService(ds);
    }
}
```

---

### @Qualifier

Specifies the bean name to inject when multiple instances of the same type exist.

| Attribute | Type     | Required | Description    |
|-----------|----------|----------|----------------|
| `value`   | `String` | Yes      | The bean name  |

| Target             | Retention |
|--------------------|-----------|
| `FIELD`, `PARAMETER` | `RUNTIME`   |

**Supported positions:**

1. **Constructor parameter** - direct named lookup

```java
@Component
public class OrderService {
    private final DataSource ds;

    public OrderService(@Qualifier("primaryDataSource") DataSource ds) {
        this.ds = ds;
    }
}
```

2. **Field** - fallback for Lombok-generated constructors

```java
@Component
@AllArgsConstructor
public class OrderService {
    @Qualifier("primaryDataSource")
    private final DataSource ds;
}
```

3. **@Instance method parameter**

```java
@Instance
public JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") DataSource ds) {
    return new JdbcTemplate(ds);
}
```

---

### @PostConstruct

Marks a method to run after the component is created and its `@Instance` methods are processed.

| Attribute | Target   | Retention |
|-----------|----------|-----------|
| *(none)*  | `METHOD` | `RUNTIME`   |

**Constraints:**
- Must return `void`
- Must have **no parameters**
- At most **1 per class**

```java
@Component
public class CacheManager {

    @PostConstruct
    public void init() {
        // warm up cache after instance creation
    }
}
```

---

### @DependsOn

Prevents a `@Component` from being instantiated or an `@Instance` method from being invoked
until all specified prerequisite beans exist.

| Attribute | Type       | Default | Description              |
|-----------|------------|---------|--------------------------|
| `value`   | `String[]` | `{}`    | Bean names to wait for   |
| `classes`  | `Class<?>[]` | `{}`  | Bean classes to wait for |

| Target           | Retention |
|------------------|-----------|
| `TYPE`, `METHOD` | `RUNTIME`   |

**All specified beans must be present.** Both `value` and `classes` can be combined.

```java
// Wait for named bean
@Component
@DependsOn("appConfig")
public class MyService { ... }

// Wait for bean classes
@Component
@DependsOn(classes = {DataSource.class, CacheManager.class})
public class ReportService { ... }

// Mix names and classes
@Component
@DependsOn(value = "primaryDataSource", classes = CacheManager.class)
public class OrderService { ... }

// On @Instance method
@Instance
@DependsOn("vertx")
public JWTAuth jwtAuth() {
    var vertx = ClassPool.getInstance("vertx", Vertx.class);
    return JWTAuth.create(vertx, ...);
}
```

---

### @Verticle

Marks a class as a Vert.x verticle for deployment.

| Attribute | Target | Retention |
|-----------|--------|-----------|
| *(none)*  | `TYPE`   | `RUNTIME`   |

Can coexist with `@Component` to also register as a managed bean.

```java
@Verticle
@Component
public class HttpVerticle extends VertxBaseVerticle { ... }
```

---

## Initialization Lifecycle

```
ClassPool.init(packagesToScan, verticleClass)
│
├─ 1. Package Scanning
│     Scan all packages + "vn.io.lcx" via PackageScanner
│     Deduplicate discovered classes
│
├─ 2. Classification
│     ├─ @Entity / @Table        -> ENTITIES list
│     ├─ @TableName              -> EntityUtils.analyzeEntityClass()
│     ├─ EntityMapping impl      -> EntityMappingContainer.addMapping()
│     ├─ @Verticle               -> verticleClass list
│     └─ @Component              -> Phase 1 or Phase 2
│
├─ 3. Phase 1 - Simple Components
│     For each @Component with no non-static fields AND @DependsOn satisfied:
│     ├─ newInstance() via no-arg constructor
│     ├─ checkProxy() -> register proxy if exists
│     ├─ setInstance() -> register in pool
│     ├─ Process @Instance methods (defer if params unresolved or @DependsOn unsatisfied)
│     └─ Invoke @PostConstruct
│     Otherwise -> defer to Phase 2
│
├─ 4. Phase 2 - Iterative Resolution Loop
│     while (progress AND unresolved items remain):
│     │
│     │  For each unresolved @Component:
│     │  ├─ Check @DependsOn -> skip if unsatisfied
│     │  ├─ resolveConstructorArgs() -> try resolve all params
│     │  └─ If all resolved -> create instance, register, process @Instance + @PostConstruct
│     │
│     │  For each deferred @Instance method:
│     │  ├─ Check @DependsOn -> skip if unsatisfied
│     │  ├─ Resolve method parameters
│     │  └─ If all resolved -> invoke and register result
│     │
│     │  progress = true if ANY item was resolved this iteration
│
└─ 5. Error Reporting
      If unresolved items remain -> ExceptionInInitializerError
      Message includes: missing fields, unsatisfied @DependsOn, unresolved parameters
```

---

## Dependency Resolution Order

When resolving a **constructor parameter** or **@Instance method parameter**, the lookup order is:

| Priority | Source                      | Example                                    |
|----------|-----------------------------|--------------------------------------------|
| 1        | `@Qualifier` on parameter   | `@Qualifier("primaryDs") DataSource ds`    |
| 2        | `@Qualifier` on field (constructor only) | Field-level `@Qualifier` for Lombok |
| 3        | Parameter name              | `getInstance("userRepository")`            |
| 4        | Parameter type (FQN)        | `getInstance("com.example.UserRepository")`|

**Lombok support:** For constructor parameters, if the parameter itself has no `@Qualifier`, the
container falls back to the matching field (by index and type) to check its `@Qualifier` annotation.

---

## Instance Registration

When a bean is registered via `setInstance(instance)`, it is stored under **multiple keys**:

| Key                          | Example                              |
|------------------------------|--------------------------------------|
| Fully qualified class name   | `com.example.service.UserService`    |
| Simple class name            | `UserService`                        |
| Superclass name (FQN)        | `com.example.service.BaseService`    |
| Superclass simple name       | `BaseService`                        |
| Interface name (FQN)         | `com.example.service.IUserService`   |
| Interface simple name        | `IUserService`                       |
| Custom name (`@Instance`)    | `"primaryDataSource"`                |
| Method name (fallback)       | `"primaryDs"` (method name)          |

**Naming conflicts:** If a key already exists, a numeric suffix is appended: `name1`, `name2`, etc.

**Named registration** via `setInstance(name, instance)` throws `DuplicateInstancesException` if
the name is already taken.

---

## ClassPool API

### Retrieving beans

```java
// By class type
UserService service = ClassPool.getInstance(UserService.class);

// By name
Object obj = ClassPool.getInstance("primaryDataSource");

// By name with type cast
DataSource ds = ClassPool.getInstance("primaryDataSource", DataSource.class);
```

### Manual registration

```java
// Register under type hierarchy (auto-naming)
ClassPool.setInstance(myInstance);

// Register under specific name + type hierarchy
ClassPool.setInstance("customName", myInstance);
```

### Configuration

```java
// Load application.yaml (called before init)
ClassPool.loadProperties();
```

---

## Common Patterns

### Simple configuration class

```java
@Component
public class GsonConfig {

    @Instance
    public Gson gson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
}
```

### Service with constructor injection

```java
@Component
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }
}
```

### Lombok + @Qualifier

```java
@Component
@AllArgsConstructor
public class DataMigrationService {
    @Qualifier("sourceDataSource")
    private final DataSource source;
    @Qualifier("targetDataSource")
    private final DataSource target;
}
```

### @DependsOn with @Instance

```java
@Component
public class AuthConfig {

    @Instance
    @DependsOn("vertx")
    public JWTAuth jwtAuth() {
        var vertx = ClassPool.getInstance("vertx", Vertx.class);
        return JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setBuffer(publicKey)));
    }
}
```

### Factory method with parameter injection

```java
@Component
public class DatabaseConfig {

    @Instance("primaryDataSource")
    public DataSource primaryDs() {
        return DataSourceBuilder.create().url("jdbc:primary").build();
    }

    @Instance("secondaryDataSource")
    public DataSource secondaryDs() {
        return DataSourceBuilder.create().url("jdbc:secondary").build();
    }

    @Instance
    public JdbcTemplate jdbcTemplate(@Qualifier("primaryDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
```

### Component with initialization logic

```java
@Component
public class CacheWarmer {

    @PostConstruct
    public void warmUp() {
        // runs after instance creation
        // load frequently accessed data into cache
    }
}
```

### Verticle as managed bean

```java
@Verticle
@Component
@DependsOn(classes = JWTAuth.class)
public class ApiVerticle extends VertxBaseVerticle {
    private final JWTAuth jwtAuth;

    public ApiVerticle(JWTAuth jwtAuth) {
        this.jwtAuth = jwtAuth;
    }
}
```

---

## Constraints & Error Messages

### Constraints

| Rule                                        | Violation error                                              |
|---------------------------------------------|--------------------------------------------------------------|
| `@Component` class must have 1 constructor  | `Class 'X' must have only 1 constructor`                    |
| `@PostConstruct` must be `void`             | `Post construct of X must be a void method`                  |
| `@PostConstruct` must have no parameters    | `Cannot create instance of X. Does not accept parameters`    |
| Max 1 `@PostConstruct` per class            | `Cannot create instance of X because there are more than one PostConstruct method` |
| `setInstance(name, obj)` with duplicate name| `DuplicateInstancesException: An instance with name X already existed with type Y` |

### Error reporting format

When the iterative loop finishes with unresolved items:

```
[Cannot create instance of class com.example.MyService [unsatisfied @DependsOn: "configBean"; missing fields: repo: com.example.Repository],
Cannot resolve @Instance method `jwtAuth` in class com.example.AuthConfig [unsatisfied @DependsOn: "vertx"],
]
```

Each entry reports:
- **unsatisfied @DependsOn** - which prerequisite beans are missing
- **missing fields** - which constructor dependencies couldn't be resolved
- **unresolved parameters** - which `@Instance` method params couldn't be resolved

---

## Compile-Time: DIScanner (Experimental - Not ready for use)

**Processor:** `vn.io.lcx.processor.DIScanner`
**Triggers on:** all classes (`@SupportedAnnotationTypes("*")`)
**Generates:** `META-INF/class-index-{UUID}.json` resource files

At compile time, `DIScanner` scans every `@Component`-annotated class and writes its metadata
to JSON resource files under `META-INF/`. Each file contains a list of `ClassInfo` objects:

```json
[
  {
    "fullQualifiedClassName": "com.example.UserService",
    "superClassesFullName": ["com.example.BaseService", "java.lang.Object"],
    "fields": [
      { "fieldName": "repo", "fieldType": "com.example.UserRepository" }
    ],
    "constructor": {
      "parameterTypes": ["com.example.UserRepository"]
    },
    "postConstruct": {
      "methodName": "init",
      "returnDataType": "void"
    },
    "createInstanceMethods": [
      { "methodName": "jwtAuth", "returnDataType": "io.vertx.ext.auth.jwt.JWTAuth" }
    ]
  }
]
```

**Collected metadata per class:**

| Field                    | Description                                                |
|--------------------------|------------------------------------------------------------|
| `fullQualifiedClassName` | Fully qualified class name                                 |
| `superClassesFullName`   | All super classes and implemented interfaces               |
| `fields`                 | All fields with name and type                              |
| `constructor`            | First constructor's parameter types                        |
| `postConstruct`          | Method annotated with `@PostConstruct` (if any)            |
| `createInstanceMethods`  | Methods annotated with `@Instance` (factory methods)       |

**How ClassPool uses these files at runtime:**

1. At startup, `ClassPool` loads all `META-INF/class-index-*.json` files from the classpath
2. Merges them into a single component registry
3. Uses the metadata for dependency resolution — constructor parameter types determine injection
   order, `@Instance` methods are detected for factory bean registration
4. This avoids expensive runtime reflection-based classpath scanning

**Why UUID in filename:** Each compilation unit (module/JAR) generates its own JSON file with a
random UUID suffix. When multiple JARs are on the classpath, all files are discovered and merged.

---

## Key Source Files

| File | Description |
|------|-------------|
| `common-lib/.../annotation/Component.java` | `@Component` annotation |
| `common-lib/.../annotation/Instance.java` | `@Instance` annotation |
| `common-lib/.../annotation/Qualifier.java` | `@Qualifier` annotation |
| `common-lib/.../annotation/PostConstruct.java` | `@PostConstruct` annotation |
| `common-lib/.../annotation/DependsOn.java` | `@DependsOn` annotation |
| `common-lib/.../annotation/Verticle.java` | `@Verticle` annotation |
| `common-lib/.../config/ClassPool.java` | DI container implementation |
| `common-lib/.../scanner/PackageScanner.java` | Package scanning (file + JAR) |
| `common-lib/.../utils/ObjectUtils.java` | `getExtendAndInterfaceClasses()` for type hierarchy |
| `common-lib/.../exception/DuplicateInstancesException.java` | Duplicate name error |
