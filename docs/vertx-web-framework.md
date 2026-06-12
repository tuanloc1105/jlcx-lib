# Vert.x Web Framework

The Vert.x layer provides compile-time generated routing, controller helpers, request/response wrappers, auth/API-key hooks, validation, and static resource serving.

## Main Packages

| Package | Role |
|---|---|
| `vn.io.lcx.vertx.base.annotation.app` | App bootstrap annotations: `@VertxApplication`, `@ComponentScan`, `@ContextHandler`. |
| `vn.io.lcx.vertx.base.annotation.process` | Controller, route, auth/API-key, and request binding annotations used by processors. |
| `vn.io.lcx.vertx.base.annotation` | Validation annotations such as `@NotNull`, `@Regex`, `@Values`, `@GreaterThan`, `@LessThan`. |
| `vn.io.lcx.vertx.base.controller` | `ReactiveController` and base helpers. |
| `vn.io.lcx.vertx.base.wrapper` | Routing/request/response wrappers. |
| `vn.io.lcx.vertx.base.validate` | Runtime validation annotations and validator. |
| `vn.io.lcx.vertx.base.verticle` | Base Verticle support. |
| `vn.io.lcx.processor` | Controller and rest-controller processors. |

## Bootstrap

Application source normally contains an app class annotated with `@VertxApplication`.

During compilation:

1. `ControllerProcessor` reads `@VertxApplication`, `@Controller`, route annotations, `@ContextHandler`, `@Auth`, and `@APIKey`.
2. It generates fixed class `vn.io.lcx.vertx.verticle.ApplicationVerticle`.
3. `RestControllerProcessor` converts `@RestController` classes into generated `Reactive{Class}` wrappers.

At runtime:

1. `MyVertxDeployment` requires `@VertxApplication`, reads `@ComponentScan`, seeds Vert.x/worker defaults, loads config, and initializes `ClassPool`.
2. `ClassPool` bootstraps components and verticles.
3. Generated `ApplicationVerticle` configures Vert.x routes and handlers.
4. Routes call generated wrappers/controllers returning `Future<T>` or response helpers.

## Controller Annotations

| Annotation | Use |
|---|---|
| `@VertxApplication` | Declares app bootstrap metadata. |
| `@ComponentScan` | Provides packages for DI/component scanning. |
| `@Controller` | Lower-level controller route class. |
| `@RestController` | Source class that gets a generated reactive wrapper. |
| `@ContextHandler` | Middleware/context handler hook. |
| `@Get`, `@Post`, `@Put`, `@Delete` | HTTP method route mappings. |
| `@Auth` | Route auth requirement. |
| `@APIKey` | API-key route requirement. |

`@RestController` is ergonomic. Generated wrappers are annotated as `@Component` and `@Controller`, extend `ReactiveController`, and delegate to original methods.

## Request Binding

Request binding annotations cover common HTTP inputs:

- path params
- query params
- headers
- form values
- body values
- file upload values

Generated route code binds these values before calling controller methods. Check the processor when changing annotation semantics, because route binding is compile-time generated.

Generated binding is mostly string-helper based for path/query/header/form values. Be careful with non-`String` parameter types unless an existing controller pattern proves the conversion path.

## ReactiveController

`ReactiveController` centralizes controller conveniences:

- read request params/headers/path/form/body/file values
- return normal JSON responses
- return file responses
- handle blocking work through Vert.x wrappers
- centralize error handling

Prefer existing helper methods before adding controller-specific parsing logic.

## Wrappers

Important wrappers:

- `RoutingContextLcxWrapper`
- `HttpServerResponseLcxWrapper`
- request/response model wrappers under `vn.io.lcx.vertx.base.http` and `model`

`RoutingContextLcxWrapper` participates in trace/operation timing and obtains Gson from `ClassPool`.

## Validation

Runtime validation uses annotations and `AutoValidation`.

Common validation annotations:

- `@NotNull`
- `@Regex`
- `@GreaterThan`
- `@LessThan`
- `@Values`

`AutoValidation` reflects fields, checks annotations, and returns/throws validation errors. Use it for DTO validation rather than duplicating checks in controllers.

## Auth And API Keys

`@Auth` and `@APIKey` are route-level annotations consumed by generated routing code.

Handlers are app-specific. Examples:

- Todo app has JWT config and auth handler under `examples/todo-app-example/src/main/java/.../config`.
- Hibernate Reactive example uses `@Auth` on task routes.

When changing auth behavior, inspect both annotations and generated `ApplicationVerticle` output.

Current gotchas:

- `@Auth` installs Vert.x JWT auth handling. User extraction and request-context population are app-specific `@ContextHandler` work.
- `@APIKey` is handled for direct `@Controller` methods, but `RestControllerProcessor` does not copy `@APIKey` from `@RestController` source methods.
- `AuthContext` is a `ThreadLocal`; prefer `RoutingContext` data such as `CommonConstant.CURRENT_USER` across async route work.

## Static Frontend Assets

Both frontend examples build Vite output into backend resources:

- `examples/todo-app-example/src/main/resources/webroot`
- `examples/hibernate-reactive-example/src/main/resources/webroot`

Generated Vert.x app code serves static assets when `@VertxApplication(staticResource = true)` is set. It uses `StaticHandler.create("webroot")` and an SPA fallback to `webroot/index.html`.

Metrics keys are split in current source:

- `server.enable-metrics` controls the generated app `/metrics` route.
- `server.metrics.enable`, `server.metrics.port`, and `server.metrics.endpoint` control embedded Vert.x/Micrometer Prometheus setup.

## HTTP Utilities

Shared client/server helpers live in:

- `HttpUtils`
- `VertxWebClientHttpUtils`
- `CommonRequest`
- request/response wrapper models

Prefer these helpers for outbound HTTP behavior to keep error/serialization behavior consistent.

## Example Routes

Todo app:

- `/api/v2/user/*`
- `/api/v2/task/*`

Hibernate Reactive example:

- `/api/users/*`
- `/api/tasks/*`

See `docs/examples.md` for exact route lists and frontend alignment issues.

## Verification

Run:

```bash
mvn -pl common-lib test
```

For route generation changes, also compile an example and inspect `target/generated-sources/annotations`.
