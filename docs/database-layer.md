# Database Layer

The database layer spans sync JDBC helpers, DDL/entity analysis, JPA repositories, Vert.x SQL reactive repositories, Hibernate Reactive repositories, pagination, and SQL specifications.

## Package Map

| Package | Role |
|---|---|
| `vn.io.lcx.common.database` | JDBC executor, properties, connection context. |
| `vn.io.lcx.common.database.utils` | Entity analysis, field processing, SQL/DDL helpers. |
| `vn.io.lcx.common.database.strategy` | DB-specific DDL strategies. |
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
| `@TableName` | Physical table name. |
| `@ColumnName` | Physical column name. |
| `@IdColumn` | Primary id column. |
| `@SecondaryIdColumn` | Secondary id column. |
| `@ForeignKey` | Foreign-key metadata. |
| `@SubTable` | Nested/sub-table mapping. |
| `@Clob` | CLOB column mapping. |
| `@Index` | Index metadata. |
| `@PreInsert` | Insert lifecycle value hook. |
| `@PreUpdate` | Update lifecycle value hook. |
| `@ReadOnly` | Exclude from write behavior. |
| `@SQLMapping` | Trigger SQL mapping generation. |
| `@AdditionalCode` | Add generated-code fragment metadata. |

These annotations are used by entity analysis, DDL generation, SQL mapping generation, and repository code.

## JDBC Executor

Main classes:

- `DatabaseExecutor`
- `DatabaseExecutorImpl`
- `DatabaseProperty`
- `ConnectionContext`

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

`PageableImpl` stores page/page-size/sort metadata. When an entity class is provided, it maps Java field names to physical column names through entity annotations.

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

- `ReactiveRepository<T, ID>`
- `SqlStatement`
- statement builders/wrappers
- result mapping utilities
- transaction/connection helpers
- retry helpers

Reactive SQL uses Vert.x SQL client types such as `SqlConnection`, `PreparedQuery`, and `Future`.

Generated code:

- `@RRepository` interfaces extending `ReactiveRepository` get `{RepositoryInterface}Impl`.
- `vn.io.lcx.reactive.annotation.Query` drives custom query methods.

The Todo example uses this path with manual SQL queries.

## Hibernate Reactive Layer

Important interface:

- `HReactiveRepository<T, ID>`

Generated code:

- `@HRRepository` interfaces extending `HReactiveRepository` get `{RepositoryInterface}Impl`.

The Hibernate Reactive example uses this path and `Stage.Session`-style APIs.

## SQL Mapping Generation

`@SQLMapping` triggers `SQLMappingProcessor`.

Generated classes:

- `{Entity}Utils`
- `{Entity}MappingImpl`

Generated mapping relies on entity annotations and templates in `common-lib/src/main/resources/template`.

See `docs/annotation-processors.md` for processor details.

## Examples

Todo app:

- prefix/config: `reactive.database.*`
- routes under `/api/v2/user` and `/api/v2/task`
- repository path: `@RRepository`
- SQL references schema names like `r_lcx`

Hibernate Reactive app:

- prefix/config: `hreactive.database.*`
- persistence unit: `postgresql-example`
- repository path: `@HRRepository`
- current `persistence.xml` appears stale against entity source

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
