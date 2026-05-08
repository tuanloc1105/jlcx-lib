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

## Mail

Important classes:

- `EmailInfo`
- `MailProperties`
- `MailHelper`
- `ReactiveMailSender`
- `MailPropertiesEmptyError`
- `MailSendingError`

`ReactiveMailSender` integrates mail sending with Vert.x blocking execution patterns.

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

## Logging

Logging resources/classes:

- `default-logback.xml`
- `LogbackConfig`
- `VertxTraceIdMDCConverter`
- `VertxOperationMDCConverter`

`default-logback.xml` includes rolling file config and placeholders such as `APPLICATION_NAME`, `LOG_PATTERN`, and `basePath`.

## Auth Context

`AuthContext` stores request/auth-related context. Be careful with async boundaries; confirm context propagation before adding new usage.

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
