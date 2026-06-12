# Configuration Reference

Configuration is loaded from YAML helpers and made available through `CommonConstant.applicationConfig`. Environment placeholders use the `${ENV_VAR:default}` style in example YAML files.

This doc records current config surfaces. Verify exact keys in the target app before changing behavior.

## Core Config Flow

| Area | Source/classes |
|---|---|
| Application config holder | `CommonConstant.applicationConfig` |
| YAML parsing | `YamlProperties`, `LCXProperties`, config helpers |
| Default JSON/XML/Gson | `DefaultConfiguration`, `BuildGson`, `BuildObjectMapper` |
| Logging | `LogbackConfig`, `default-logback.xml` |
| DI bootstrap | `ClassPool` |

`common-lib` does not ship a main `application.yaml`. Examples and tests provide app configs. `ClassPool.loadProperties()` reads classpath `application.yaml` or the system property `application_config.file`.

Despite names such as `PropertiesUtils`, current source handles `.yaml`/`.yml`, not Java `.properties` files. Missing non-placeholder values can become the literal string `"null"`; several config paths compare against `CommonConstant.NULL_STRING`.

`ClassPool.loadProperties()` also reads `json.sensitive_field` and appends those names to `JsonMaskingUtils.CUSTOM_FIELD`.

## Common Server Keys

Examples use server keys for host/port style configuration. Generated `ApplicationVerticle` consumes `@VertxApplication` metadata plus config values.

Common things to check:

- `server.port`
- `server.enable-http-2`
- `server.body-bytes-limit`
- `server.cookie-auth-name`
- `server.api-key`
- `server.enable-metrics`
- `server.metrics.enable`, `server.metrics.port`, `server.metrics.endpoint`
- `server.enable-virtual-thread`

See `docs/vertx-web-framework.md` for routing behavior.

## JDBC Database Config

Sync/JPA paths use `DatabaseProperty` and `DBTypeEnum`.

Supported database types:

- `ORACLE`
- `POSTGRESQL`
- `MYSQL`
- `SQL_SERVER`

Typical properties:

- `server.database.host`
- `server.database.port`
- `server.database.username`
- `server.database.password`
- `server.database.name`
- `server.database.schema_name`
- `server.database.driver_class_name`
- `server.database.dialect`
- `server.database.initial_pool_size`
- `server.database.max_pool_size`
- `server.database.max_timeout`
- `server.database.type`
- `server.database.use_cache`

`DBTypeEnum` stores driver class, JDBC URL template, version SQL, and Hibernate dialect information.

## Reactive SQL Config

Source reads the `server.reactive.database.*` prefix. Example YAML nests this as `server.reactive.database` and uses env defaults:

| Key | Env default family |
|---|---|
| `server.reactive.database.host` | `REACTIVE_DATABASE_HOST` |
| `server.reactive.database.port` | `REACTIVE_DATABASE_PORT` |
| `server.reactive.database.username` | `REACTIVE_DATABASE_USERNAME` |
| `server.reactive.database.password` | `REACTIVE_DATABASE_PASSWORD` |
| `server.reactive.database.name` | `REACTIVE_DATABASE_NAME` |
| `server.reactive.database.max_pool_size` | `REACTIVE_DATABASE_MAX_POOL_SIZE` |
| `server.reactive.database.type` | `REACTIVE_DATABASE_TYPE` |

Current Todo default port is `6060` and default DB type is PostgreSQL.

## Hibernate Reactive Config

Hibernate Reactive source reads `server.hreactive.database.*` and `META-INF/persistence.xml`. Example YAML nests this as `server.hreactive.database`, but the env placeholders are still named `REACTIVE_DATABASE_*`.

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

Known reactive Redis keys:

- `server.reactive.redis.host`
- `server.reactive.redis.port`
- `server.reactive.redis.password`

Gotcha: `VertxRedisConfiguration.redis()` currently reads max pool size from `server.reactive.database.max_pool_size`, not `server.reactive.redis.max_pool_size`.

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
