# Utilities And Shared Infrastructure

`common-lib` contains more than small helper methods. It also provides shared cache, mail, cron, lock, task, logging, and context infrastructure.

## Utility Packages

| Package | Role |
|---|---|
| `vn.io.lcx.common.utils` | General-purpose utilities. |
| `vn.io.lcx.common.cache` | Redis pool and cache helpers. |
| `vn.io.lcx.common.mail` | Mail properties, email model, mail helper, reactive sender. |
| `vn.io.lcx.common.cron` | Cron expression parser and field model. |
| `vn.io.lcx.common.lock` | Lock manager. |
| `vn.io.lcx.common.thread` | Executor and virtual-thread support. |
| `vn.io.lcx.common.task` | Batch and retry task helpers. |
| `vn.io.lcx.common.logging` | Logback MDC converters. |
| `vn.io.lcx.common.context` | Auth/request context. |
| `vn.io.lcx.common.scanner` | Package scanning. |
| `vn.io.lcx.common.array` | `LargeArray`. |
| `vn.io.lcx.common.exception` | Framework exceptions. |
| `vn.io.lcx.common.constant` | Shared constants. |

## General Utilities

The source currently has 28 utility classes under `vn.io.lcx.common.utils`.

Common areas:

- string and word-case helpers
- object/null helpers
- collection helpers
- JSON masking and serialization helpers
- YAML/properties loading
- date/time helpers
- random/UUIDv7 helpers
- file helpers
- number formatting
- exception helpers
- crypto/hash helpers: RSA, AES, BCrypt, simple cipher
- HTTP/socket helpers
- topological sort

Before adding a helper, search this package first. Many small operations already exist.

## Cache

Important classes:

- `RedisPool`
- `RedisPoolImpl`
- `CacheUtils`
- `CacheException`

`RedisPoolImpl` handles Redis pool behavior such as CRUD/ping-style operations. Treat Redis config keys as app-specific unless confirmed in target YAML.

`CacheUtils<K,V>` is an in-memory cache with capacity and scheduled expiry cleanup. Reactive Redis client setup lives under `vn.io.lcx.reactive.cache.VertxRedisConfiguration`.

## Mail

Important classes:

- `EmailInfo`
- `MailProperties`
- `MailHelper`
- `ReactiveMailSender`
- `MailPropertiesEmptyError`
- `MailSendingError`

`ReactiveMailSender` wraps blocking SMTP send through Vert.x `WorkerExecutor.executeBlocking(..., false)`.

`MailHelper` validates host/port/username/password/email targets, forces SMTP auth/starttls/SSL socket factory/TLSv1.2 and 10s timeouts, then sleeps 500ms between messages.

## Cron

Cron parser/model classes:

- `CronExpression`
- `CronFieldType`
- `BasicField`
- `SimpleField`
- `DayOfMonthField`
- `DayOfWeekField`
- `FieldPart`

The parser supports cron field handling, including optional seconds behavior in current source.

## Thread And Task Helpers

Important classes/interfaces:

- `BaseExecutor`
- `SimpleExecutor`
- `LcxThreadFactory`
- `VirtualThreadSupport`
- `BatchHandler`
- `MyTaskRetrying`
- `RejectMode`

`DefaultConfiguration` registers executor-related defaults and can use virtual-thread-aware support when runtime allows.

`MyTaskRetrying` retries with `Thread.sleep` and returns `null` after exhausting retries rather than rethrowing the last exception.

`LockManager` is file-lock based. It appends `.lock` when missing, retries every 100ms until timeout, throws if the same instance already holds a lock, and deletes the lock file on release.

## Logging

Logging resources/classes:

- `default-logback.xml`
- `LogbackConfig`
- `VertxTraceIdMDCConverter`
- `VertxOperationMDCConverter`

`default-logback.xml` includes rolling file config and placeholders such as `APPLICATION_NAME`, `LOG_PATTERN`, and `basePath`.

## Auth Context

`AuthContext` is a static `ThreadLocal<Object>` with `set`, `get`, typed `get`, and `clear`. Be careful with async boundaries; prefer `RoutingContext` storage for route-local user data.

## Constants

Important constants:

- `CommonConstant`
- `JavaSqlResultSetConstant`

`CommonConstant.applicationConfig` is the global application config access point used throughout the framework.

## Exceptions

Framework exceptions include DI, config, cache, mail, database, reactive, and Vert.x error types.

When adding a new exception, keep it close to the package that owns the behavior. Do not add generic exception types unless several callers share the same semantics.

## Processor Support Utilities

Processor helper classes live under `vn.io.lcx.processor.*`, even though they are in `common-lib`.

Important areas:

- `template.CodeTemplates`
- `model.FieldMappingInfo`
- `model.SourceParameterInfo`
- mapper/generator services
- codegen utilities such as `ReactiveCodeGenHelper`

See `docs/annotation-processors.md` before editing them.

## Tests

Utility and infra tests are mostly under `common-lib/src/test/java/vn/io/lcx/common`.

Run:

```bash
mvn -pl common-lib test
```
