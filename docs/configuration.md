# Configuration Reference

Configuration is loaded from YAML/properties helpers and made available through `CommonConstant.applicationConfig`. Environment placeholders use the `${ENV_VAR:default}` style in example YAML files.

This doc records current config surfaces. Verify exact keys in the target app before changing behavior.

## Core Config Flow

| Area | Source/classes |
|---|---|
| Application config holder | `CommonConstant.applicationConfig` |
| YAML/property parsing | `YamlProperties`, `LCXProperties`, config helpers |
| Default JSON/XML/Gson | `DefaultConfiguration`, `BuildGson`, `BuildObjectMapper` |
| Logging | `LogbackConfig`, `default-logback.xml` |
| DI bootstrap | `ClassPool` |

`common-lib` does not ship a main `application.yaml`. Examples and tests provide app configs.

## Common Server Keys

Examples use server keys for host/port style configuration. `ControllerProcessor` and generated `ApplicationVerticle` consume `@VertxApplication` metadata plus config values.

Common things to check:

- HTTP port
- static webroot
- component scan package
- context handlers
- auth/API-key handlers
- Vert.x deployment options

See `docs/vertx-web-framework.md` for routing behavior.

## JDBC Database Config

Sync/JPA paths use `DatabaseProperty` and `DBTypeEnum`.

Supported database types:

- `ORACLE`
- `POSTGRESQL`
- `MYSQL`
- `SQL_SERVER`

Typical properties:

- host
- port
- username
- password
- database name/service name
- max pool size or connection settings
- database type

`DBTypeEnum` stores driver class, JDBC URL template, version SQL, and Hibernate dialect information.

## Reactive SQL Config

Todo app uses the `reactive.database.*` prefix with env defaults:

| Key | Env default family |
|---|---|
| `reactive.database.host` | `REACTIVE_DATABASE_HOST` |
| `reactive.database.port` | `REACTIVE_DATABASE_PORT` |
| `reactive.database.username` | `REACTIVE_DATABASE_USERNAME` |
| `reactive.database.password` | `REACTIVE_DATABASE_PASSWORD` |
| `reactive.database.database` / name | `REACTIVE_DATABASE_NAME` |
| `reactive.database.max-pool-size` | `REACTIVE_DATABASE_MAX_POOL_SIZE` |
| `reactive.database.type` | `REACTIVE_DATABASE_TYPE` |

Current Todo default port is `6060` and default DB type is PostgreSQL.

## Hibernate Reactive Config

Hibernate Reactive example uses the `hreactive.database.*` prefix and `META-INF/persistence.xml`.

Current app facts:

- server port: `5050`
- default DB name: `hreact`
- persistence unit: `postgresql-example`
- provider: `org.hibernate.reactive.provider.ReactivePersistenceProvider`

Known mismatch: `persistence.xml` lists `Author` and `Book`, while current source contains `UsersEntity` and `TasksEntity`.

## Cache

Redis helpers live under `vn.io.lcx.common.cache`.

Important classes:

- `RedisPool`
- `RedisPoolImpl`
- `CacheUtils`

Check app config for host/port/password/database/pool naming before use; current docs should not invent missing keys.

## Mail

Mail helpers live under `vn.io.lcx.common.mail`.

Important classes:

- `MailProperties`
- `EmailInfo`
- `MailHelper`
- `ReactiveMailSender`

`ReactiveMailSender` sends through Vert.x blocking execution wrappers.

## Logging

`default-logback.xml` supports placeholders including:

- `APPLICATION_NAME`
- `LOG_PATTERN`
- `basePath`

Custom converters:

- `vMdcTrace`
- `vMdcOperation`

Related classes:

- `VertxTraceIdMDCConverter`
- `VertxOperationMDCConverter`

## Frontend Config

Todo app frontend:

- `VITE_BACKEND_API_URL` controls backend base URL.
- `.env.example` currently says `api/v1`, but backend routes use `/api/v2/...`.

Hibernate Reactive frontend:

- Vite dev proxy maps `/api` to `http://localhost:5050`.
- Build output goes to backend `src/main/resources/webroot`.

## Deploy Config Gotchas

- Todo Helm chart values currently use `DATABASE_*` names, while app YAML expects `REACTIVE_DATABASE_*`.
- Todo Dockerfile uses Java 11 base image while library source targets Java 17.
- Example configs include hardcoded local DB defaults and demo RSA keys. Treat as development-only.
