# Vert.x Web Framework

## Overview

The framework provides annotation-driven HTTP routing on top of Vert.x 5.0.7. At compile
time, annotation processors generate an `ApplicationVerticle` that wires controllers, filters,
authentication handlers, and the HTTP server together.

All web annotations are in package `vn.com.lcx.vertx.base.annotation`.

---

## Application Bootstrap

### @VertxApplication

Marks the main application class. Required for the `ControllerProcessor` to generate the
`ApplicationVerticle`.

| Attribute        | Type      | Default | Description                    |
|------------------|-----------|---------|--------------------------------|
| `staticResource` | `boolean` | `false` | Serve static files from `webroot/` |

| Target | Retention |
|--------|-----------|
| `TYPE` | `RUNTIME` |

### @ComponentScan

Specifies additional packages to scan for `@Component` classes.

| Attribute | Type       | Default                   | Description             |
|-----------|------------|---------------------------|-------------------------|
| `value`   | `String[]` | main class package only   | Packages to scan        |

| Target | Retention |
|--------|-----------|
| `TYPE` | `RUNTIME` |

### Startup

```java
@VertxApplication
@ComponentScan({"com.example.app", "com.example.shared"})
public class Application {
    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(Application.class);
    }
}
```

`MyVertxDeployment.deployVerticle()` performs:

1. Load logback config (or default)
2. Load `application.yaml` via `ClassPool.loadProperties()`
3. Create `Vertx` instance (with Micrometer metrics if enabled)
4. Create shared worker executor (`lcx-vert.x-worker-pool`, pool size 20, 5 min timeout)
5. Print application banner
6. Scan components from annotated packages
7. Deploy all `@Verticle`-annotated classes

---

## Controllers

### @Controller (SOURCE retention)

Marks a class as an HTTP controller. Methods annotated with `@Get`, `@Post`, `@Put`, or
`@Delete` are discovered at compile time and wired into the generated `ApplicationVerticle`.

| Attribute | Type     | Default | Description              |
|-----------|----------|---------|--------------------------|
| `path`    | `String` | `""`    | Base path for all routes |

```java
@Controller(path = "/api/v1/users")
@Component
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Get(path = "/list")
    public void listUsers(RoutingContext ctx) { ... }

    @Post(path = "/create")
    @Auth
    public void createUser(RoutingContext ctx) { ... }
}
```

### @RestController (RUNTIME retention)

Combines `@Component` and `@Controller` behavior. The `RestControllerProcessor` generates a
reactive wrapper class that handles JSON serialization, request validation, and error handling
automatically.

| Attribute | Type     | Default | Description              |
|-----------|----------|---------|--------------------------|
| `path`    | `String` | `""`    | Base path for all routes |

```java
@RestController(path = "/api/v2/tasks")
@Component
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Post(path = "/create")
    @Auth
    public Future<CommonResponse> create(
            RoutingContext ctx,
            @RequestBody CreateTaskRequest req) {
        return taskService.create(ctx, req)
                .map(v -> new CommonResponse());
    }
}
```

**Key differences from `@Controller`:**
- Methods return `Future<T>` for async responses
- Parameters support `@RequestBody`, `@RequestParam`, `@PathVariable`, etc.
- Request validation runs automatically
- JSON serialization/deserialization is handled by the generated wrapper

---

## HTTP Method Annotations

All have `SOURCE` retention and `METHOD` target.

| Annotation | HTTP Method | Attribute                          |
|------------|-------------|------------------------------------|
| `@Get`     | GET         | `String path()` (default `""`)     |
| `@Post`    | POST        | `String path()` (default `""`)     |
| `@Put`     | PUT         | `String path()` (default `""`)     |
| `@Delete`  | DELETE      | `String path()` (default `""`)     |

Paths are concatenated: `controller.path + method.path`. Leading slashes are normalized
automatically.

---

## Request Parameter Binding

All have `RUNTIME` retention and `PARAMETER` target.

### @RequestBody

Binds the HTTP request body (parsed as JSON) to a method parameter.

```java
@Post(path = "/create")
public Future<CommonResponse> create(
        RoutingContext ctx,
        @RequestBody CreateTaskRequest req) { ... }
```

### @RequestParam

Binds a query string parameter.

| Attribute      | Type      | Default | Description                     |
|----------------|-----------|---------|---------------------------------|
| `value`        | `String`  | `""`    | Query param name                |
| `required`     | `boolean` | `true`  | Throw if missing                |
| `defaultValue` | `String`  | `""`    | Fallback when missing/empty     |

```java
@Get(path = "/search")
public Future<SearchResponse> search(
        RoutingContext ctx,
        @RequestParam(value = "q") String query,
        @RequestParam(value = "page", required = false, defaultValue = "1") int page) { ... }
```

### @PathVariable

Binds a URI template variable.

| Attribute | Type     | Default | Description           |
|-----------|----------|---------|-----------------------|
| `value`   | `String` | `""`    | Path variable name    |

```java
@Get(path = "/:taskId")
public Future<TaskResponse> getById(
        RoutingContext ctx,
        @PathVariable("taskId") String taskId) { ... }
```

### @RequestHeader

Binds an HTTP request header.

| Attribute      | Type      | Default | Description                     |
|----------------|-----------|---------|---------------------------------|
| `value`        | `String`  | `""`    | Header name                     |
| `required`     | `boolean` | `true`  | Throw if missing                |
| `defaultValue` | `String`  | `""`    | Fallback value                  |

```java
@Post(path = "/action")
public Future<Void> action(
        RoutingContext ctx,
        @RequestHeader("Authorization") String token) { ... }
```

### @RequestForm

Binds a form attribute from `application/x-www-form-urlencoded` or `multipart/form-data`.

| Attribute | Type     | Default | Description             |
|-----------|----------|---------|-------------------------|
| `value`   | `String` | `""`    | Form attribute name     |

### @RequestFile

Binds a file upload from a multipart request. The parameter type should be
`io.vertx.ext.web.FileUpload`.

| Attribute | Type     | Default | Description              |
|-----------|----------|---------|--------------------------|
| `value`   | `String` | `""`    | File parameter name      |

---

## Security Annotations

### @Auth (SOURCE retention)

Adds a JWT authentication handler before the route. The generated `ApplicationVerticle`
creates a `JWTAuthHandler` from a `JWTAuth` bean in the `ClassPool`.

```java
@Post(path = "/protected")
@Auth
public Future<CommonResponse> protectedEndpoint(RoutingContext ctx) { ... }
```

### @APIKey (SOURCE retention)

Adds API key validation before the route. The key is read from the `x-api-key` header and
validated against `server.api-key` in `application.yaml`.

```java
@Post(path = "/webhook")
@APIKey
public Future<Void> webhook(RoutingContext ctx) { ... }
```

---

## Middleware / Filters

### @ContextHandler (SOURCE retention)

Marks a class as a middleware filter. The class must implement `VertxContextHandler`.

| Attribute | Type  | Default             | Description        |
|-----------|-------|---------------------|--------------------|
| `order`   | `int` | `Integer.MIN_VALUE` | Execution priority |

Multiple filters are sorted by `order` (ascending) and inserted into the handler chain
before the controller method.

```java
@ContextHandler(order = 100)
@Component
public class CorsFilter implements VertxContextHandler {
    @Override
    public void handle(RoutingContext ctx) {
        ctx.response()
            .putHeader("Access-Control-Allow-Origin", "*");
        ctx.next();
    }
}
```

---

## Handler Chain

Every route has a handler chain assembled in this order:

```
BodyHandler (parses request body)
  ↓
JWTAuthHandler           (if @Auth)
  ↓
API Key Validator         (if @APIKey)
  ↓
createUUIDHandler         (always — injects trace ID)
  ↓
@ContextHandler filters   (in order)
  ↓
Controller method
```

The `createUUIDHandler` generates a UUIDv7 trace ID and stores it in the routing context
under the key `trace_id`.

---

## Request Validation

### Annotation-Based Validation

Validation annotations are applied to request DTO fields. They have `RUNTIME` retention and
`FIELD` target.

| Annotation     | Attribute        | Behavior                              |
|----------------|------------------|---------------------------------------|
| `@NotNull`     | —                | Must not be null or empty             |
| `@GreaterThan` | `double value()` | Numeric field must be `> value`       |
| `@LessThan`    | `double value()` | Numeric field must be `< value`       |
| `@Regex`       | `String value()` | String must match pattern             |
| `@Values`      | `String[] value()` | String must be one of allowed values |

```java
@Data
public class CreateTaskRequest {
    @NotNull
    private String taskName;

    @GreaterThan(0)
    private int priority;

    @Values({"LOW", "MEDIUM", "HIGH"})
    private String severity;

    @Regex("^[a-zA-Z0-9_]+$")
    private String tag;
}
```

### AutoValidation

`AutoValidation.validate(Object)` is called automatically for `@RestController` request
objects. It:

1. Iterates all declared fields (skips `static` and `final`)
2. Checks each validation annotation
3. Recursively validates nested objects, `Collection` elements, `Map` values, and `Optional`
4. Skips classes from `java.*`, `org.*`, `io.*`, `jakarta.*`, `net.*`, `redis.*`, and enums
5. Returns a `List<String>` of error messages

Field names in error messages use `@SerializedName` value if present, otherwise the Java
field name.

### Custom Validation

Request DTOs can implement `CommonRequest` to provide custom validation logic:

```java
public class AdvancedRequest implements CommonRequest {
    private String startDate;
    private String endDate;

    @Override
    public void validate() {
        if (LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
    }
}
```

---

## Response Types

### CommonResponse

Standard response wrapper with error tracking:

```java
public class CommonResponse implements Serializable {
    private String trace;
    private int errorCode;
    private String errorDescription;
    private int httpCode;
}
```

### PageableResponse\<T\>

Paginated response:

```java
PageableResponse<TaskDTO> response = PageableResponse.create(
    taskList, totalElements, pageNumber, pageSize);
// Fields: pageNumber, pageSize, totalPages, numberOfElements,
//         totalElements, firstPage, lastPage, content
```

### ResponseEntity\<T\>

A generic response wrapper that lets you override the HTTP status code alongside any response
payload. When `ReactiveController.handleResponse()` detects the return value is a
`ResponseEntity`, it extracts the status and body before serialization — so the status you set
here becomes the actual HTTP status code of the response.

**Class structure:**

```java
public class ResponseEntity<T> {
    private int status;
    private T response;
}
```

**Constructors:**

```java
new ResponseEntity<>(201, dto)
```

**Static factory methods:**

| Method                              | Status | Description                         |
|-------------------------------------|--------|-------------------------------------|
| `ResponseEntity.of(int, T)`        | custom | Create with any status and payload  |
| `ResponseEntity.ok(T)`             | 200    | Shorthand for success               |
| `ResponseEntity.badRequest(String)` | 400    | Bad request with error message      |
| `ResponseEntity.internalServerError(String)` | 500 | Server error with message      |

**How it works in the response pipeline:**

In `ReactiveController.handleResponse()`, when the response object is a `ResponseEntity<?>`:

1. The `status` field overrides the default HTTP status code
2. The `response` field becomes the actual body to serialize
3. If the inner `response` is a `CommonResponse`, the framework still populates `trace`,
   `errorCode`, `errorDescription`, and `httpCode` automatically
4. The resolved body is then serialized to JSON (via Gson or Jackson) and sent

```java
// Example: return 201 Created with a DTO body
@Post(path = "/create")
public Future<ResponseEntity<TaskDTO>> create(RoutingContext ctx, @RequestBody CreateTaskRequest req) {
    return taskService.create(req)
        .map(dto -> ResponseEntity.of(201, dto));
}

// Example: return 200 OK shorthand
@Get(path = "/:id")
public Future<ResponseEntity<TaskDTO>> getById(RoutingContext ctx, @PathVariable String id) {
    return taskService.findById(id)
        .map(ResponseEntity::ok);
}

// Example: return 400 Bad Request
@Post(path = "/validate")
public Future<ResponseEntity<String>> validate(RoutingContext ctx, @RequestBody SomeRequest req) {
    if (!isValid(req)) {
        return Future.succeededFuture(ResponseEntity.badRequest("Invalid input"));
    }
    return Future.succeededFuture(ResponseEntity.ok("Valid"));
}
```

### FileEntity

Represents a file that should be streamed as the HTTP response instead of being serialized to
JSON. When `ReactiveController` detects the response object is a `FileEntity`, it uses Vert.x's
`sendFile()` to stream the file directly, bypassing JSON serialization entirely.

**Class structure:**

```java
public class FileEntity {
    private final String filePath;        // absolute path to the file
    private final boolean deleteAfterSend; // remove file after streaming completes
}
```

**Constructors:**

```java
new FileEntity("/path/to/file.pdf")                // deleteAfterSend = false
new FileEntity("/path/to/file.pdf", true)           // deleteAfterSend = true
```

**Builder pattern:**

```java
FileEntity file = FileEntity.builder()
    .filePath("/tmp/report.pdf")
    .deleteAfterSend(true)
    .build();
```

**How it works in the response pipeline:**

In `ReactiveController.returnResponse()`, when the response object is a `FileEntity`:

1. Sets the HTTP status code, `Processed-Time`, and `Trace` headers
2. Sets `Content-Type` to `application/octet-stream` if no `Content-Type` header was already set
3. Streams the file using `response.sendFile(filePath)`
4. On success, if `deleteAfterSend` is `true`, the file is deleted asynchronously using
   `Files.deleteIfExists()` inside a Vert.x blocking executor
5. On failure, an error is logged (level `ERROR`)

**Response headers for file responses:**

| Header          | Value                                          |
|-----------------|------------------------------------------------|
| `Content-Type`  | `application/octet-stream` (if not already set)|
| `Processed-Time`| Current date-time (`yyyy-MM-dd HH:mm:ss.SSS`) |
| `Trace`         | Trace ID from routing context                  |

```java
// Example: download a file
@Get(path = "/download/:fileId")
public Future<FileEntity> download(RoutingContext ctx, @PathVariable String fileId) {
    return fileService.resolve(fileId)
        .map(path -> new FileEntity(path, true)); // auto-delete after send
}

// Example: using the builder
@Get(path = "/export")
public Future<FileEntity> export(RoutingContext ctx) {
    return reportService.generateReport()
        .map(path -> FileEntity.builder()
            .filePath(path)
            .deleteAfterSend(true)
            .build());
}
```

---

## JSON Handling

The framework supports both **Gson** and **Jackson** for serialization. The JSON handler is
resolved from `ClassPool` — whichever is registered as a bean.

| Operation       | Gson                              | Jackson                                |
|-----------------|-----------------------------------|----------------------------------------|
| Deserialize     | `gson.fromJson(reader, TypeToken)` | `objectMapper.readValue(body, JavaType)` |
| Serialize       | `gson.toJson(resp)`               | `objectMapper.writeValueAsString(resp)` |
| Strictness      | `LENIENT`                         | Default                                |

Response headers always include:

| Header          | Value                                                      |
|-----------------|------------------------------------------------------------|
| `Content-Type`  | `application/json`                                         |
| `Processed-Time`| Current date-time (`yyyy-MM-dd HH:mm:ss.SSS`)             |
| `Trace`         | Trace ID from routing context                              |

---

## Error Handling

### ErrorCode Interface

```java
public interface ErrorCode {
    int getHttpCode();
    int getCode();
    String getMessage();
}
```

### Built-in Error Codes

| Enum              | HTTP | Code   | Message          |
|-------------------|------|--------|------------------|
| `SUCCESS`         | 200  | 100000 | Success          |
| `INVALID_REQUEST` | 400  | 100001 | Invalid request  |
| `INTERNAL_ERROR`  | 500  | 100002 | Internal error   |
| `DATA_NOT_FOUND`  | 404  | 100003 | Data not found   |
| `DATA_ERROR`      | 400  | 100004 | Data error       |

### InternalServiceException

Throw this from any layer to produce an error response with a specific HTTP code:

```java
throw new InternalServiceException(ErrorCodeEnums.DATA_NOT_FOUND, "User not found");
// Response: HTTP 404, {"errorCode": 100003, "errorDescription": "Data not found; User not found"}
```

---

## Logging & Wrappers

### RoutingContextLcxWrapper

Wraps the native `RoutingContext` to add request/response logging:

- Logs request URL, method, headers, and payload on initialization
- Masks sensitive JSON fields (passwords, tokens, etc.) using `JsonMaskingUtils`
- Calculates and logs API processing duration on response
- Truncates response payloads larger than 10,000 characters in logs

### HttpServerResponseLcxWrapper

Wraps `HttpServerResponse` to log response details including status code, headers, and
payload size.

### MyRouter / MyRouterImpl

Wraps Vert.x `Router` to log route registration at startup:

```
Configuring [GET]    path [/api/v1/users/list]
Configuring [POST]   path [/api/v1/users/create]
```

### EmptyRoutingContext

Minimal `RoutingContext` implementation for testing or background processing. Provides only
basic key-value data storage; all HTTP operations throw `NotImplementedException`.

```java
EmptyRoutingContext ctx = EmptyRoutingContext.init();
ctx.put("trace_id", "abc123");
```

---

## HTTP Server Configuration

Properties in `application.yaml`:

| Property                       | Default | Description                         |
|--------------------------------|---------|-------------------------------------|
| `server.port`                  | `8080`  | Listen port                         |
| `server.body-bytes-limit`      | —       | Max request body size (bytes)       |
| `server.enable-http-2`         | `false` | Enable HTTP/2 (h2c cleartext)       |
| `server.enable-metrics`        | `false` | Enable Prometheus `/metrics`        |
| `server.enable-virtual-thread` | `false` | Use virtual threads (Java 21+)      |
| `server.api-key`               | —       | Expected value for `@APIKey`        |

### Built-in Endpoints

| Path              | Description                      |
|-------------------|----------------------------------|
| `/health`         | Returns `OK` (HTTP 200)          |
| `/starting_probe` | Returns `OK` (HTTP 200)          |
| `/metrics`        | Prometheus scraping (if enabled) |

### HTTP/2 Support

Enable cleartext HTTP/2 (h2c):

```yaml
server:
  enable-http-2: true
```

For HTTP/2 with TLS (h2), register a custom `HttpServerOptions` bean in `ClassPool`:

```java
@Component
public class ServerConfig {
    @Instance
    public HttpServerOptions httpServerOptions() {
        return HttpOption.configureHttp2TlsH2(8443, "keystore.jks", "password");
    }
}
```

---

## Key Source Files

| File                                          | Description                          |
|-----------------------------------------------|--------------------------------------|
| `vertx/base/annotation/app/VertxApplication.java` | Application marker annotation    |
| `vertx/base/annotation/app/ComponentScan.java`    | Package scan configuration       |
| `vertx/base/annotation/app/ContextHandler.java`   | Middleware filter annotation      |
| `vertx/base/annotation/process/Controller.java`   | Controller annotation            |
| `vertx/base/annotation/process/RestController.java` | REST controller annotation     |
| `vertx/base/annotation/process/Get.java`         | GET route mapping                 |
| `vertx/base/annotation/process/Post.java`        | POST route mapping                |
| `vertx/base/annotation/process/Put.java`         | PUT route mapping                 |
| `vertx/base/annotation/process/Delete.java`      | DELETE route mapping              |
| `vertx/base/annotation/process/Auth.java`        | JWT auth annotation               |
| `vertx/base/annotation/process/APIKey.java`      | API key auth annotation           |
| `vertx/base/annotation/process/RequestBody.java` | Request body binding              |
| `vertx/base/annotation/process/RequestParam.java`| Query param binding               |
| `vertx/base/annotation/process/PathVariable.java`| Path variable binding             |
| `vertx/base/annotation/process/RequestHeader.java`| Header binding                   |
| `vertx/base/annotation/process/RequestForm.java` | Form attribute binding            |
| `vertx/base/annotation/process/RequestFile.java` | File upload binding               |
| `vertx/base/controller/ReactiveController.java`  | Base controller with helpers      |
| `vertx/base/verticle/VertxBaseVerticle.java`      | Base verticle class               |
| `vertx/base/custom/MyVertxDeployment.java`        | Application deployment            |
| `vertx/base/custom/MyRouterImpl.java`             | Router with logging               |
| `vertx/base/wrapper/RoutingContextLcxWrapper.java`| Request/response logging          |
| `vertx/base/validate/AutoValidation.java`         | Annotation-based validation       |
| `vertx/base/config/HttpOption.java`               | HTTP/2 configuration              |
| `processor/ControllerProcessor.java`              | Route code generation             |
| `processor/RestControllerProcessor.java`          | REST wrapper code generation      |
