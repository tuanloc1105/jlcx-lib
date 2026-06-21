# Agent Project Map

Use this file for repo navigation. For domain behavior, prefer the narrower docs in `docs/`.

## Source Of Truth

- `CLAUDE.md` is the writable agent guide.
- `AGENTS.md` and `GEMINI.md` are symlinks to `CLAUDE.md`.
- Dependency and compiler versions come from `pom.xml`.
- Root Maven modules are only `common-lib` and `processor`; examples are independent projects under `examples/`.

## Main Library

| Area | Paths | First read |
|---|---|---|
| DI and configuration | `common-lib/src/main/java/vn/io/lcx/common/config/`, especially `common-lib/src/main/java/vn/io/lcx/common/config/ClassPool.java` | `docs/classpool-di-container.md` |
| Common annotations and scanning | `common-lib/src/main/java/vn/io/lcx/common/annotation/`, `common-lib/src/main/java/vn/io/lcx/common/scanner/` | `docs/project-overview.md` |
| JDBC/database helpers | `common-lib/src/main/java/vn/io/lcx/common/database/` | `docs/database-layer.md` |
| JPA repositories | `common-lib/src/main/java/vn/io/lcx/jpa/` | `docs/database-layer.md` |
| Reactive SQL | `common-lib/src/main/java/vn/io/lcx/reactive/` | `docs/database-layer.md` |
| Vert.x web framework | `common-lib/src/main/java/vn/io/lcx/vertx/base/` | `docs/vertx-web-framework.md` |
| Utilities and shared infra | `common-lib/src/main/java/vn/io/lcx/common/{utils,cache,mail,cron,lock,task,thread,logging,context}/` | `docs/utilities.md` |

## Annotation Processors

- Real processor implementations: `common-lib/src/main/java/vn/io/lcx/processor/`.
- Processor templates: `common-lib/src/main/resources/template/`.
- Facade module: `processor/src/main/java/vn/io/lcx/processor/ProcessorModule.java`.
- SPI registration: `processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor`.
- Processor tests: `common-lib/src/test/java/vn/io/lcx/processor/`.

Read `docs/annotation-processors.md` before changing processors or templates. Generated Java goes under `target/generated-sources/annotations/`; do not edit it directly.

## Backend/API/Data Examples

| Area | Paths | Routing |
|---|---|---|
| Todo controllers | `examples/todo-app-example/src/main/java/com/example/lcx/controller/` | `docs/examples.md`, `docs/vertx-web-framework.md` |
| Todo services | `examples/todo-app-example/src/main/java/com/example/lcx/service/` | `docs/classpool-di-container.md` |
| Todo repositories | `examples/todo-app-example/src/main/java/com/example/lcx/respository/` | `docs/database-layer.md` |
| Todo entities/DTOs | `examples/todo-app-example/src/main/java/com/example/lcx/entity/`, `examples/todo-app-example/src/main/java/com/example/lcx/object/` | `docs/database-layer.md` |
| Hibernate Reactive controllers | `examples/hibernate-reactive-example/src/main/java/com/example/controller/` | `docs/examples.md` |
| Hibernate Reactive services | `examples/hibernate-reactive-example/src/main/java/com/example/service/` | `docs/classpool-di-container.md` |
| Hibernate Reactive repositories/models | `examples/hibernate-reactive-example/src/main/java/com/example/repository/`, `examples/hibernate-reactive-example/src/main/java/com/example/model/` | `docs/database-layer.md` |
| Auth handlers/config | `examples/todo-app-example/src/main/java/com/example/lcx/config/`, `examples/hibernate-reactive-example/src/main/java/com/example/handler/`, `examples/hibernate-reactive-example/src/main/java/com/example/config/` | `docs/vertx-web-framework.md`, `docs/configuration.md` |

There are no source `.sql` migration directories. Schema behavior is annotation/helper driven through `common-lib/src/main/java/vn/io/lcx/common/database/utils/`.

## Frontend Examples

Frontend code exists only in examples.

### Todo UI

- App routes/shell: `examples/todo-app-example/web/src/App.tsx`.
- Mount/config: `examples/todo-app-example/web/src/main.tsx`, `examples/todo-app-example/web/vite.config.ts`, `examples/todo-app-example/web/package.json`, `examples/todo-app-example/web/.env.example`.
- Pages/components: `examples/todo-app-example/web/src/components/`.
- State/data fetching: `examples/todo-app-example/web/src/context/AppProvider.tsx`, `examples/todo-app-example/web/src/utils/api-utils.ts`, `examples/todo-app-example/web/src/dto/`.
- Styling/assets: `examples/todo-app-example/web/src/index.css`, `examples/todo-app-example/web/src/App.css`, `examples/todo-app-example/web/public/`.

### Hibernate Reactive UI

- App routes/shell: `examples/hibernate-reactive-example/web/src/App.tsx`.
- Mount/config: `examples/hibernate-reactive-example/web/src/main.tsx`, `examples/hibernate-reactive-example/web/vite.config.ts`, `examples/hibernate-reactive-example/web/package.json`, `examples/hibernate-reactive-example/web/tsconfig.app.json`.
- Pages/guards: `examples/hibernate-reactive-example/web/src/pages/`, `examples/hibernate-reactive-example/web/src/components/ProtectedRoute.tsx`.
- State/data fetching: `examples/hibernate-reactive-example/web/src/context/AuthContext.tsx`, `examples/hibernate-reactive-example/web/src/services/`.
- Components/design utilities: `examples/hibernate-reactive-example/web/src/components/ui/`, `examples/hibernate-reactive-example/web/src/components/theme-provider.tsx`, `examples/hibernate-reactive-example/web/src/lib/utils.ts`.
- Styling/assets: `examples/hibernate-reactive-example/web/src/index.css`, `examples/hibernate-reactive-example/web/src/App.css`, `examples/hibernate-reactive-example/web/public/`.

No frontend test harness is checked in. Use lint/build checks from `docs/agent/workflows.md`.

## Generated And Artifact Paths

Do not edit these unless the user explicitly asks:

- `target/`
- `*/target/generated-sources/annotations/`
- frontend build output such as `dist/` or `build/`
- generated gRPC stubs wherever the gRPC build writes them, including `target/generated-sources/` or generated `com/example/grpc/` packages if present

For gRPC API changes, edit `examples/grpc-example/proto/hello.proto`, then run the relevant gRPC build script.
