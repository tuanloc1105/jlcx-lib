# Agent Operations Notes

Use this file for config, deployment, logging, and operational surfaces. The repository mainly contains library code plus example deployments, not production runbooks.

## Configuration

Read `docs/configuration.md` before changing config behavior.

Core config readers and defaults:

- `common-lib/src/main/java/vn/io/lcx/common/config/ClassPool.java`
- `common-lib/src/main/java/vn/io/lcx/common/utils/PropertiesUtils.java`
- `common-lib/src/main/java/vn/io/lcx/common/constant/CommonConstant.java`
- `common-lib/src/test/resources/application-test.yaml`

Example app config:

- `examples/todo-app-example/src/main/resources/application.yaml`
- `examples/todo-app-example/web/.env.example`
- `examples/hibernate-reactive-example/src/main/resources/application.yaml`
- `examples/hibernate-reactive-example/src/main/resources/META-INF/persistence.xml`

Framework config keys include `server.` prefixes in source, including `server.database.*`, `server.reactive.database.*`, and `server.hreactive.database.*`.

## Database Runtime

Read `docs/database-layer.md` before changing database behavior.

Useful implementation entrypoints:

- `common-lib/src/main/java/vn/io/lcx/reactive/config/ReactiveDbClientConfiguration.java`
- `common-lib/src/main/java/vn/io/lcx/reactive/config/ReactiveHibernateConfiguration.java`
- `common-lib/src/main/java/vn/io/lcx/common/database/utils/`
- `common-lib/src/main/java/vn/io/lcx/jpa/respository/`
- `common-lib/src/main/java/vn/io/lcx/reactive/repository/`

There are no Flyway/Liquibase migration directories. DDL/query behavior is implemented in helpers and generated repository code.

## Docker And Helm

Deployment examples are limited to the Todo app.

Docker:

- `examples/todo-app-example/Dockerfile`

Helm:

- `examples/todo-app-example/charts/Chart.yaml`
- `examples/todo-app-example/charts/values.yaml`
- `examples/todo-app-example/charts/templates/`

Known mismatch: the Todo Helm chart uses `DATABASE_*` names, while app config expects `REACTIVE_DATABASE_*`. The Todo Dockerfile currently uses a Java 11 base image while the project targets Java 17.

## Secrets And Demo Keys

Example resources contain local/demo defaults and RSA keys. Do not present them as production-safe.

Inspect before changing:

- `examples/todo-app-example/charts/templates/secret.yaml`
- `examples/todo-app-example/src/main/resources/key/key.key`
- `examples/todo-app-example/src/main/resources/key/key.pub`
- `examples/hibernate-reactive-example/src/main/resources/key/key.key`
- `examples/hibernate-reactive-example/src/main/resources/key/key.pub`

## Logging And Observability

Read `docs/utilities.md` and `docs/configuration.md` first.

Main logging/observability paths:

- `common-lib/src/main/resources/default-logback.xml`
- `common-lib/src/main/java/vn/io/lcx/common/config/LogbackConfig.java`
- `common-lib/src/main/java/vn/io/lcx/common/logging/VertxTraceIdMDCConverter.java`
- `common-lib/src/main/java/vn/io/lcx/common/logging/VertxOperationMDCConverter.java`
- `common-lib/src/main/java/vn/io/lcx/vertx/base/custom/MyVertxDeployment.java`

No dedicated Prometheus/Grafana manifests are checked in.

## Release Surface

Release scripts exist at the repo root:

- `snapshot.sh`, `snapshot.ps1`
- `release.sh`, `release.ps1`
- `deploy.ps1`

They assume local tool paths and Nexus-style deployment configuration. Inspect scripts and `pom.xml` before editing release behavior.
