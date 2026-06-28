# Database Layer

The database layer spans sync JDBC helpers, DDL/entity analysis, JPA repositories, Vert.x SQL reactive repositories, Hibernate Reactive repositories, pagination, and SQL specifications.

## Package Map

| Package | Role |
|---|---|
| `vn.io.lcx.common.database` | JDBC executor, properties, connection context. |
| `vn.io.lcx.common.database.utils` | Entity analysis, field processing, SQL/DDL helpers, and DB-specific DDL strategies. |
| `vn.io.lcx.common.database.pageable` | DB-specific pagination. |
| `vn.io.lcx.common.database.specification` | Fluent SQL condition builder. |
| `vn.io.lcx.common.database.handler.statement` | Input statement parameter handlers. |
| `vn.io.lcx.common.database.handler.statement.output` | Result set/output handlers. |
| `vn.io.lcx.jpa.*` | JPA annotations, repository contract, entity/session helpers. |
| `vn.io.lcx.reactive.*` | Vert.x SQL repository contracts, SQL statements, mapping, transaction helpers. |

## Database Types

`DBTypeEnum` supports:

- Oracle
- PostgreSQL
- MySQL
- SQL Server

It stores database driver, JDBC URL template, database version SQL, and Hibernate dialect metadata.

## Entity Annotations

Important annotations:

| Annotation | Use |
|---|---|
| `@TableName` | Physical table name plus table-level index metadata. |
| `@ColumnName` | Physical column name plus insert/update/nullability, `index`, and `columnDataTypeDefinition` metadata. |
| `@IdColumn` | Primary id column. |
| `@SecondaryIdColumn` | Secondary id column. |
| `@ForeignKey` | Foreign-key metadata: `referenceColumn`, `referenceTable`, and `cascade`. |
| `@SubTable` | Nested/sub-table mapping. |
| `@Clob` | CLOB column mapping. |
| `@Index` | Index metadata. |
| `@PreInsert` | Insert lifecycle value hook. |
| `@PreUpdate` | Update lifecycle value hook. |
| `@ReadOnly` | Exclude from write behavior. |
| `@SQLMapping` | Trigger SQL mapping generation. |
| `@SQLProjection` | Trigger read-only SQL result row mapping generation for DTO/projection classes. |
| `@AdditionalCode` | Add generated-code fragment metadata. |

These annotations are used by entity analysis, DDL generation, SQL mapping generation, and repository code.

## JDBC Executor

Main classes:

- `DatabaseExecutor`
- `DatabaseExecutorImpl`
- `DatabaseProperty`
- `ConnectionContext`

`ConnectionContext` is deprecated for removal in current source; prefer the executor/config paths used by the active repository layers.

`DatabaseExecutorImpl` provides singleton-style JDBC operations:

- query execution with `PreparedStatement`
- mutation/update execution
- batch execution
- Oracle/PostgreSQL stored procedure calls with `CallableStatement`
- centralized statement/result-set cleanup

PostgreSQL stored procedure paths include commit behavior in current source.

## Entity Analysis And DDL

Important classes:

- `DBEntityAnalysis`
- `EntityUtils`
- `FieldProcessor`
- `DatabaseStrategy`
- `DatabaseStrategyFactory`
- `OracleStrategy`
- `PostgreSQLStrategy`
- `MySQLStrategy`
- `MSSQLStrategy`

Strategies generate DB-specific SQL for:

- table creation/update
- id columns
- indexes
- sequences
- foreign keys and cascade behavior
- rename/add/drop/modify column

Before changing annotation semantics, check both entity analysis and all database strategies.

## Pagination

Core interface/class:

- `Pageable`
- `PageableImpl`

DB-specific implementations:

- Oracle pageable
- PostgreSQL pageable
- MySQL pageable
- SQL Server pageable

`PageableImpl` stores page/page-size/sort metadata and is a base holder. Several rendering/mapping methods are intentionally unimplemented there; vendor pageables perform SQL rendering and field-to-column mapping.

## Specifications

`Specification` and `SimpleSpecificationImpl` build SQL conditions fluently.

Supported operation families include:

- `where`, `and`, `or`
- equality and inequality
- `in`
- `like`
- `between`
- comparison operators
- null checks

Specification code also maps entity fields to column names when entity metadata is available.

`SimpleSpecificationImpl` is marked in source as still being tested; be conservative when changing or relying on edge-case predicate behavior.

## JPA Layer

Important packages/classes:

- `vn.io.lcx.jpa.annotation`
- `JpaRepository<E, ID>`
- JPA repository helpers under the actual source package `vn.io.lcx.jpa.respository`
- transaction/session/entity context helpers

Important annotations:

- `@Repository`
- `@Service`
- `@Transactional`
- `@Query`
- `@Param`
- `@Modifying`
- `@ResultSetMapping`

Generated code:

- `@Repository` interfaces extending `JpaRepository` get `{RepositoryInterface}Proxy`.
- `@Service` classes get `{ServiceClass}Proxy` with transaction-aware behavior.

Do not silently correct the package typo `respository`; it is the current source path.

## Reactive SQL Layer

Important classes/interfaces:

- `ReactiveRepository<T>`
- `SqlStatement`
- statement builders/wrappers
- result mapping utilities
- transaction/connection helpers
- retry helpers

Reactive SQL uses Vert.x SQL client types such as `SqlConnection`, `PreparedQuery`, and `Future`.

Generated code:

- `@RRepository` interfaces extending `ReactiveRepository` get `{RepositoryInterface}Impl`.
- `vn.io.lcx.reactive.annotation.Query` drives custom query methods.
- Custom methods without supported generated behavior need reactive `@Query`.
- Custom query method signatures are compile-time enforced: `Future<X>` return, `RoutingContext` first, `SqlConnection` second, and `Pageable` only as the final parameter.
- Query placeholders support `?` or `?1` styles, but not both in one query. `IN (?)` and `IN (?1)` expand collection and object-array parameters.
- `Future<Page<T>>` uses explicit `@Query(countQuery = "...")` when provided. Simple select queries can derive `SELECT COUNT(1)`, while complex SQL requires an explicit count query.
- `Future<List<T>>` with `Pageable` and `Future<Object[]>` are deprecated and emit processor warnings.

The Todo example uses this path with manual SQL queries.

## Hibernate Reactive Layer

Important interfaces and annotations:

- `HReactiveRepository<T>`
- `@HRRepository`
- `@HRQuery`
- `@HRParam`
- `@HRModifying`
- `@HRResultSetMapping`

Generated code:

- `@HRRepository` interfaces extending `HReactiveRepository` get `{RepositoryInterface}Impl`.
- Repository methods use `Stage.Session` as the first parameter and return `Future<X>`.
- Custom Hibernate Reactive methods must use `@HRQuery`; JPA `@Query` and Vert.x reactive SQL `@Query` are rejected for HR repositories.
- Supported query returns are `Future<T>`, `Future<List<T>>`, `Future<Optional<T>>`, `Future<Page<T>>`, and for `@HRModifying` only, `Future<Integer>` or `Future<Void>`.
- Query placeholders can be implicit `?`, indexed `?1`, or named `:name`; named placeholders require `@HRParam`, and mixed placeholder styles fail at compile time.
- `Future<Page<T>>` requires final `Pageable`. Use `@HRQuery(countQuery = "...")` for complex queries such as `WITH`, `UNION`, `DISTINCT`, `GROUP BY`, `HAVING`, or fetch joins.
- Native DTO/entity mappings use `@HRResultSetMapping` with `@HRQuery(isNative = true)`.

The Hibernate Reactive example uses this path and `Stage.Session`-style APIs. If old HR code imports `vn.io.lcx.jpa.annotation.Query`, migrate it to `vn.io.lcx.reactive.annotation.HRQuery`.

## SQL Mapping Generation

`@SQLMapping` triggers `SQLMappingProcessor` for entity-like classes.

Generated classes:

- `{Entity}Utils`
- `{Entity}MappingImpl`

Generated mapping relies on entity annotations and templates in `common-lib/src/main/resources/template`.

The processor validates the mapping contract at compile time. A mapped entity must have a non-blank `@TableName`, exactly one non-static/non-final `@IdColumn`, supported mapped field types, matching getters and setters, at least one insertable column, and at least one updatable non-id column. `@PreInsert` and `@PreUpdate` methods must be parameterless `void` methods; the generated statement methods call them before building insert/update SQL.

Generated SQL honors `@ColumnName(name = ...)`, `insertable`, `updatable`, and `nullable` for statement and parameter generation. Generated JDBC and Vert.x row mapping fails fast by throwing `IllegalStateException` with column/field/entity context when a column read or conversion fails.

`@SQLProjection` is for read-only DTOs returned by custom SQL queries. It uses the same row mapping behavior and `@ColumnName(name = "...")` aliases, but it only generates `{Projection}Utils` with `resultSetMapping(ResultSet)` and `vertxRowMapping(Row)`. It does not require `@TableName` or `@IdColumn`, and it does not generate insert/update/delete helpers. `@ReadOnly` is not equivalent; it controls repository write behavior for entity mappings.

```java
@SQLProjection
public class DisputeProcessInfo {
    @ColumnName(name = "ID")
    private BigDecimal id;

    @ColumnName(name = "PROCESS_ID")
    private BigDecimal processId;
}
```

For joined queries, alias columns to match the projection mapping:

```sql
SELECT ndi.id AS ID, ndi.process_id AS PROCESS_ID
FROM ...
```

See `docs/annotation-processors.md` for processor details.

## Examples

Todo app:

- source config prefix: `server.reactive.database.*` (example YAML nests this as `server.reactive.database`)
- routes under `/api/v2/user` and `/api/v2/task`
- repository path: `@RRepository`
- SQL references schema names like `r_lcx`
- entities use `@SQLMapping`/`@TableName` style mapping

Hibernate Reactive app:

- source config prefix: `server.hreactive.database.*` (example YAML nests this as `server.hreactive.database`)
- persistence unit: `postgresql-example`
- repository path: `@HRRepository`
- current `persistence.xml` appears stale against entity source
- entities use standard Jakarta `@Entity`/`@Table` mapping

See `docs/examples.md`.

## Verification

Run focused tests for the changed area:

```bash
mvn -pl common-lib test
```

For generated repository changes:

```bash
mvn clean install
```

Then compile an example that uses the changed path.
