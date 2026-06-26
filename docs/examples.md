# Examples Guide

The `examples/` directory contains independent projects. They are not root Maven modules.

## Overview

| Example | Purpose |
|---|---|
| `todo-app-example` | Vert.x backend using reactive SQL repositories, React/Vite frontend, Dockerfile, and Helm chart. |
| `hibernate-reactive-example` | Todo backend using Hibernate Reactive repositories, React/Vite frontend. |
| `grpc-example` | Multi-module gRPC client/server with shared proto generation. |

Example configs include local defaults and committed demo keys. Treat them as development samples, not production-safe defaults.

## Todo App Example

Path: `examples/todo-app-example`

Backend:

- main class: `com.example.lcx.App`
- app annotation: `@VertxApplication`
- server port: `6060`
- data access: `@RRepository` plus custom SQL
- auth: `@Auth` on task routes, JWT config under `src/main/java/com/example/lcx/config/`, demo keys under `src/main/resources/key/`
- current repository package typo: `respository`

Routes:

| Area | Routes |
|---|---|
| User | `POST /api/v2/user/create_new`, `POST /api/v2/user/login` |
| Task | `POST /api/v2/task/create_task`, `get_task_detail`, `search_tasks_by_name`, `get_all_task`, `update_task`, `delete_task`, `delete_tasks`, `mark_task_as_finished` |

Main entities:

- `TaskEntity`: id, task name/detail, remind time, finished flag, user id, audit fields
- `UserEntity`: id, username, password, full name, audit fields, status

Config keys:

- source reads `server.reactive.database.*`; YAML nests it under `server.reactive.database`
- env defaults use `REACTIVE_DATABASE_*`
- default database type is PostgreSQL

Frontend:

- path: `examples/todo-app-example/web`
- React 19, Vite, TypeScript, Ant Design, Tailwind 4, Axios, React Router 7
- build output: `../src/main/resources/webroot`; treat it as generated frontend output
- scripts: `pnpm dev`, `pnpm build`, `pnpm lint`, `pnpm preview`

Gotchas:

- `.env.example` uses `VITE_BACKEND_API_URL=api/v1`, but backend routes are `/api/v2/...`. Align it before running.
- Dockerfile uses a Java 11 base image while project source targets Java 17.
- Helm chart values use `DATABASE_*` style names, while the app config expects `REACTIVE_DATABASE_*`; verify env mapping before deployment.
- RSA keys are committed for demo use under `src/main/resources/key`.

Common commands:

```bash
cd examples/todo-app-example
./build.sh
./clean.sh
cd web && pnpm install && pnpm run build
```

## Hibernate Reactive Example

Path: `examples/hibernate-reactive-example`

Backend:

- main class: `com.example.App`
- server port: `5050`
- data access: `@HRRepository`
- auth: `@Auth` on task routes
- persistence provider: `org.hibernate.reactive.provider.ReactivePersistenceProvider`
- persistence unit: `postgresql-example`

Routes:

| Area | Routes |
|---|---|
| User | `POST /api/users/create_new`, `POST /api/users/login` |
| Task | `POST /api/tasks/create_task`, `get_task_detail`, `search_task_by_name`, `get_all_task`, `update_task`, `delete_task` |

Main entities:

- `TasksEntity`: table `todo.tasks`, task title/detail, finished flag, audit timestamps, user relation
- `UsersEntity`: table `todo.users`, username/password/full name, audit timestamps, task relation

Config keys:

- source reads `server.hreactive.database.*`; YAML nests it under `server.hreactive.database`
- env placeholders are still named `REACTIVE_DATABASE_*`
- server port `5050`
- default database name `hreact`

Frontend:

- path: `examples/hibernate-reactive-example/web`
- React 19, Vite, TypeScript, Tailwind 4, Radix/shadcn-style components, React Hook Form, Zod, Sonner, Lucide, Axios, React Router 7
- Vite proxy maps `/api` to `http://localhost:5050`
- build output: `../src/main/resources/webroot`

Gotchas:

- `META-INF/persistence.xml` currently lists `Author` and `Book`, while current source contains `UsersEntity` and `TasksEntity`. Verify before relying on ORM boot.
- `start.sh` runs `hibernate-reactive-example-1.0.0.jar` from the current directory; Maven normally emits under `target/`.
- RSA keys are committed for demo use under `src/main/resources/key`.

Common commands:

```bash
cd examples/hibernate-reactive-example
./build.sh
./start.sh
./stop.sh
./clean.sh
cd web && pnpm install && pnpm run build
```

## gRPC Example

Path: `examples/grpc-example`

Structure:

- `grpc-client/`
- `grpc-server/`
- `proto/hello.proto`

Proto:

- proto package: `helloworld`
- Java package: `com.example.grpc`
- service: `Greeter`
- RPC: `SayHello(HelloRequest) returns (HelloReply)`

Server:

- main class: `com.example.App`
- `GreeterServiceImpl` is `@Component`
- `GrpcServerVerticle` is `@Component`
- current source binds the gRPC HTTP server on port `7070`

Client:

- main class: `com.example.App`
- creates a client for `localhost:7070` and calls the generated service

Generation:

- `build.sh` runs `protoc` twice, once for server and once for client.
- Requires `DEV_KIT_LOCATION/tool/protoc-gen-grpc-java`.
- Requires `DEV_KIT_LOCATION/tool/protoc-gen-vertx`.
- `note.txt` contains PowerShell variants.
- Older README text may mention `9090`; the current source uses `7070`.

Commands:

```bash
cd examples/grpc-example
./build.sh
./clean.sh
mvn clean compile
cd grpc-server && mvn exec:java -Dexec.mainClass="com.example.App"
cd grpc-client && mvn exec:java -Dexec.mainClass="com.example.App"
```

## Root Scripts

Root scripts build library modules, not examples:

```bash
./build.sh
./clean.sh
./snapshot.sh
./release.sh
```

Use example-local scripts for example apps.
