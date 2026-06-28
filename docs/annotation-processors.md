# Annotation Processors

The project exposes 9 annotation processors. Current packaging is subtle:

- Implementations live in `common-lib/src/main/java/vn/io/lcx/processor`.
- The `processor` module is a facade/registrar jar with `ProcessorModule` and `META-INF/services/javax.annotation.processing.Processor`.
- `processor/pom.xml` depends on `common-lib`, so ServiceLoader sees service names from `processor` and loads implementation classes from transitive `common-lib`.

When configuring annotation processor paths manually, include both `processor` and its transitive `common-lib` resources. Templates are loaded from `common-lib/src/main/resources/template`.

## Processor Table

| Processor | Supported annotations | Generated output | Notes |
|---|---|---|---|
| `ControllerProcessor` | `@ContextHandler`, `@Controller`, `@VertxApplication` | `vn.io.lcx.vertx.verticle.ApplicationVerticle` | One fixed generated verticle per app compile. Combines app config, context handlers, routes, static resources, auth, and API-key hooks. |
| `RestControllerProcessor` | `@RestController` | `Reactive{OriginalClass}` | Same package. Generated wrapper is `@Component`, `@Controller`, extends `ReactiveController`, and delegates to the original controller. |
| `RepositoryProcessor` | `@Repository` | `{RepositoryInterface}Proxy` | JPA repository proxy for interfaces extending `JpaRepository`. |
| `ServiceProcessor` | `@Service` | `{ServiceClass}Proxy` | Transaction-aware service proxy. |
| `ReactiveRepositoryProcessor` | `@RRepository` | `{RepositoryInterface}Impl` | Vert.x SQL repository implementation. |
| `HRRepositoryProcessor` | `@HRRepository` | `{RepositoryInterface}Impl` | Hibernate Reactive repository implementation. |
| `MapperClassProcessor` | `@MapperClass` | `{MapperInterface}Impl` | Object mapper implementation using `@Mapping`, `@Mappings`, `@Merging`, and mapper config. |
| `SQLMappingProcessor` | `@SQLMapping`, `@SQLProjection` | `{Entity}Utils`, `{Entity}MappingImpl`; `{Projection}Utils` | SQL/entity mapping helpers and read-only projection row mappers. |
| `DIScanner` | wildcard `*`; filters `@Component` root elements | `META-INF/class-index-{UUID}.json` | Compile-time component metadata. Current runtime DI does not read these index files. |

All processors return `SourceVersion.latest()` while the project targets Java 17 through Maven.

## SPI Registration

`processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor` registers:

```text
vn.io.lcx.processor.MapperClassProcessor
vn.io.lcx.processor.SQLMappingProcessor
vn.io.lcx.processor.ReactiveRepositoryProcessor
vn.io.lcx.processor.ServiceProcessor
vn.io.lcx.processor.RepositoryProcessor
vn.io.lcx.processor.ControllerProcessor
vn.io.lcx.processor.DIScanner
vn.io.lcx.processor.RestControllerProcessor
vn.io.lcx.processor.HRRepositoryProcessor
```

No equivalent service file exists under `common-lib/src/main/resources`.

## Templates

Generator templates live in `common-lib/src/main/resources/template`:

- `controller-template.txt`
- `vertx-verticle-template.txt`
- `repository-template.txt`
- `service-template.txt`
- `method-template.txt`
- `jpa-method-template.txt`
- `jpa-do-work-method-template.txt`
- `jpa-criteria-handler-template.txt`
- `sql-mapping-template.txt`
- `entity-mapping-impl-template.txt`

Template changes can affect multiple generated classes. Verify with processor tests and example compilation.

## Controller Generation

`ControllerProcessor` reads:

- `@VertxApplication` for bootstrap configuration
- `@Controller` route classes and route methods
- HTTP method annotations: `@Get`, `@Post`, `@Put`, `@Delete`
- `@ContextHandler` middleware
- auth annotations: `@Auth`, `@APIKey`
- request binding annotations for path/query/header/form/body/file values

Generated output is fixed: `vn.io.lcx.vertx.verticle.ApplicationVerticle`.

`@ComponentScan` is read by `MyVertxDeployment` at runtime when it builds the package list for `ClassPool`; do not model it as `ControllerProcessor` input.

## Rest Controller Generation

`RestControllerProcessor` wraps classes annotated with `@RestController`.

Generated class shape:

- name: `Reactive{OriginalClass}`
- same package as source controller
- annotated as `@Component` and `@Controller`
- extends `ReactiveController`
- delegates endpoint methods to original controller logic

Use this path for REST controller ergonomics; use raw `@Controller` for lower-level control.

## Repository And Service Generation

JPA:

- `@Repository` interfaces extending `JpaRepository<E, ID>` get `{Name}Proxy`.
- `@Service` classes get `{Name}Proxy`, including transaction handling.
- `@Transactional`, `@Query`, `@Param`, `@Modifying`, and `@ResultSetMapping` influence generated repository/service behavior.

Reactive SQL:

- `@RRepository` interfaces extending `ReactiveRepository<T>` get `{Name}Impl`.
- `vn.io.lcx.reactive.annotation.Query` drives custom SQL methods.
- Custom query methods are compile-time validated: return `Future<X>`, first parameter `RoutingContext`, second parameter `SqlConnection`, and final `Pageable` only when pagination is used.
- Query placeholders support either implicit `?` order or indexed `?1` order. Mixed placeholder styles fail compilation.
- `IN (?)` and `IN (?1)` expand `Collection` and object-array parameters into database-specific placeholders and tuple values.
- `Future<Page<T>>` methods use `@Query(countQuery = "...")` when present; simple selects can derive a count query, while complex SQL such as `WITH`, `UNION`, `DISTINCT`, `GROUP BY`, or `HAVING` must define one.
- `Future<List<T>>` with `Pageable` and `Future<Object[]>` still compile but emit deprecation warnings.

Hibernate Reactive:

- `@HRRepository` interfaces extending `HReactiveRepository<T>` get `{Name}Impl`.
- Generated code works with `Stage.Session` style Hibernate Reactive APIs.
- Custom query methods use `vn.io.lcx.reactive.annotation.HRQuery`, not JPA `@Query` or reactive SQL `@Query`.
- HR query parameters use `@HRParam` for named placeholders, modifying queries use `@HRModifying`, and native result mappings use `@HRResultSetMapping`.
- Supported HR custom returns are `Future<T>`, `Future<List<T>>`, `Future<Optional<T>>`, `Future<Page<T>>`, plus `Future<Integer>` or `Future<Void>` for `@HRModifying`.
- Placeholders support implicit `?`, indexed `?1`, or named `:name`; mixed styles and placeholder/parameter mismatches fail during annotation processing.
- `Future<Page<T>>` methods require final `Pageable`; simple queries can derive a count query, while complex queries should use `@HRQuery(countQuery = "...")`.
- Migrating HR repositories from older examples means replacing `vn.io.lcx.jpa.annotation.Query` with `vn.io.lcx.reactive.annotation.HRQuery`.

## Mapper Generation

`MapperClassProcessor` reads mapper source annotations:

- `@MapperClass`
- `@MapperConfig`
- `@Mapping`
- `@Mappings`
- `@Merging`

Support classes live under `vn.io.lcx.processor.model`, `service`, and `utility`.
Tests for field mapping and code generation live in `common-lib/src/test/java/vn/io/lcx/processor`.

### Multi-Parameter Mapper Methods

`MapperClassProcessor` supports mapper methods with more than one source parameter:

```java
ObjectC map(ObjectA a, ObjectB b);
```

Auto-mapping scans target fields against source parameters in declaration order. A target field is mapped from the first source parameter that has the same raw field name and exact field type.

Use explicit mappings when a target field should read from a specific parameter:

```java
@Mapping(fromParameter = "b", fromField = "fieldInB", toField = "fieldInTarget")
ObjectC map(ObjectA a, ObjectB b);
```

If `fromParameter` is blank on an explicit mapping, the first source parameter is used. Invalid `fromParameter`, missing `fromField`, missing `toField`, and duplicate explicit target fields fail during annotation processing instead of relying on generated Java compile errors. Multi-source mapping returns `null` when any source parameter is `null`.

## SQL Mapping Generation

`SQLMappingProcessor` reads `@SQLMapping` on entity-like classes and emits:

- `{Entity}Utils`
- `{Entity}MappingImpl`

It directly uses:

- `@TableName` for the target table and optional schema.
- `@ColumnName` for physical column names plus `insertable`, `updatable`, and `nullable` generated SQL/parameter behavior.
- `@IdColumn` for the single primary id column.
- `@Clob` for JDBC CLOB-to-string result mapping.
- `@PreInsert` and `@PreUpdate` for lifecycle hooks called when generated insert/update statements are built.

`@SQLMapping` classes must compile with a non-blank `@TableName`, exactly one non-static/non-final `@IdColumn`, supported field types, JavaBean-style accessors, at least one insertable column, and at least one updatable non-id column. `@PreInsert` and `@PreUpdate` methods must be unique per annotation, parameterless, and return `void`.

Generated result-set and Vert.x row mapping is fail-fast: a missing column, type mismatch, invalid enum value, or other mapping failure is rethrown as an `IllegalStateException` with entity, field, and column context. Enum database `NULL` values map to Java `null`.

`@SQLProjection` is for read-only query DTOs populated from custom SQL results, joins, subqueries, or views. It emits only `{Projection}Utils` with `resultSetMapping(ResultSet)` and `vertxRowMapping(Row)`, uses `@ColumnName(name = "...")` for result column names or SQL aliases, and does not require `@TableName` or `@IdColumn`. It does not generate table/id/write statement helpers.

Example:

```java
@SQLProjection
public class DisputeProcessInfo {
    @ColumnName(name = "ID")
    private BigDecimal id;

    @ColumnName(name = "PROCESS_ID")
    private BigDecimal processId;
}
```

For joined queries, alias selected columns to the projection column names:

```sql
SELECT
    ndi.id AS ID,
    ndi.process_id AS PROCESS_ID,
    npt.original_processing_code AS ORIGINAL_PROCESSING_CODE
FROM ...
```

`@ReadOnly` is not a projection marker; it controls repository write behavior for entity mappings.

Related database metadata annotations such as `@SecondaryIdColumn`, `@ForeignKey`, `@SubTable`, `@Index`, and `@ReadOnly` are used by the broader database analysis/DDL/helper layer; this processor does not currently consume them directly.

## Debugging Generated Code

1. Run a focused compile with annotation processing enabled.
2. Inspect `target/generated-sources/annotations`.
3. Check processor errors before generated Java errors; template failures often surface later.
4. Verify service registration and processor classpath if javac reports processor class load failures.
5. If `DIScanner` output is missing, check whether the component was a root element in the current compile round.

## Verification

Useful commands:

```bash
mvn -pl common-lib test
mvn -pl processor test
mvn clean install
```

Processor tests currently live in `common-lib`, not `processor`.
