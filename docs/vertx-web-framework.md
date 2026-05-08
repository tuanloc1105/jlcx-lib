# Vert.x Web Framework

The Vert.x layer provides compile-time generated routing, controller helpers, request/response wrappers, auth/API-key hooks, validation, and static resource serving.

## Main Packages

| Package | Role |
|---|---|
| `vn.io.lcx.vertx.base.annotation.process` | Source-level app/controller/route annotations used by processors. |
| `vn.io.lcx.vertx.base.annotation.request` | Request binding annotations. |
| `vn.io.lcx.vertx.base.annotation.check` | Auth/API-key annotations. |
| `vn.io.lcx.vertx.base.controller` | `ReactiveController` and base helpers. |
| `vn.io.lcx.vertx.base.wrapper` | Routing/request/response wrappers. |
| `vn.io.lcx.vertx.base.validate` | Runtime validation annotations and validator. |
| `vn.io.lcx.vertx.base.verticle` | Base Verticle support. |
| `vn.io.lcx.processor` | Controller and rest-controller processors. |

## Bootstrap

Application source normally contains an app class annotated with `@VertxApplication`.

During compilation:

1. `ControllerProcessor` reads `@VertxApplication`, `@ComponentScan`, `@Controller`, route annotations, `@ContextHandler`, `@Auth`, and `@APIKey`.
2. It generates fixed class `vn.io.lcx.vertx.verticle.ApplicationVerticle`.
3. `RestControllerProcessor` converts `@RestController` classes into generated `Reactive{Class}` wrappers.

At runtime:

1. `ClassPool` bootstraps components.
2. Generated `ApplicationVerticle` configures Vert.x routes and handlers.
3. Routes call generated wrappers/controllers returning `Future<T>` or response helpers.

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

## Static Frontend Assets

Both frontend examples build Vite output into backend resources:

- `examples/todo-app-example/src/main/resources/webroot`
- `examples/hibernate-reactive-example/src/main/resources/webroot`

Generated Vert.x app code can serve static assets depending on app config/annotation settings.

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
