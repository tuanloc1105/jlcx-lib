# Database Layer

## Overview

The database layer supports Oracle, PostgreSQL, MySQL, and SQL Server through:

- **JDBC execution** - `DatabaseExecutor` for direct SQL queries and stored procedures
- **Entity annotations** - `@TableName`, `@ColumnName`, `@IdColumn` for entity-to-table mapping
- **DDL generation** - Strategy pattern generates database-specific migration SQL
- **ORM integration** - JPA via Hibernate ORM (sync) and Hibernate Reactive (async)
- **Repository pattern** - `JpaRepository` with criteria queries and pagination
- **Reactive support** - `EntityMapping` for Vert.x SQL client row/tuple mapping
- **Code generation** - `SQLMappingProcessor` generates entity utils at compile time

---

## Database Configuration

### DatabaseProperty

Configuration holder for JDBC connections:

| Field                | Type      | Description                    |
|----------------------|-----------|--------------------------------|
| `connectionString`   | `String`  | JDBC URL                       |
| `username`           | `String`  | Database username              |
| `password`           | `String`  | Database password              |
| `driverClassName`    | `String`  | JDBC driver class              |
| `initialPoolSize`    | `int`     | Initial connection pool size   |
| `maxPoolSize`        | `int`     | Maximum connection pool size   |
| `maxTimeout`         | `int`     | Connection timeout (seconds)   |
| `showSql`            | `boolean` | Log executed SQL               |
| `showSqlParameter`   | `boolean` | Log SQL parameters             |

### DBType / DBTypeEnum

Database type definitions:

| Enum         | Driver                                        | URL Template                                          | Dialect                              |
|--------------|-----------------------------------------------|-------------------------------------------------------|--------------------------------------|
| `ORACLE`     | `oracle.jdbc.OracleDriver`                    | `jdbc:oracle:thin:@//%s:%d/%s`                        | `OracleDialect`                      |
| `POSTGRESQL` | `org.postgresql.Driver`                        | `jdbc:postgresql://%s:%d/%s`                           | `PostgreSQLDialect`                  |
| `MYSQL`      | `com.mysql.cj.jdbc.Driver`                     | `jdbc:mysql://%s:%d/%s`                                | `MySQLDialect`                       |
| `MSSQL`      | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | `jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false` | `SQLServerDialect`                   |

---

## JDBC Execution

### DatabaseExecutor Interface

Core interface for all database operations. Implemented by `DatabaseExecutorImpl` (singleton).

**Query execution:**

```java
<T> List<T> executeQuery(
    Connection connection,
    String sqlString,
    Map<Integer, Object> parameter,
    ResultSetHandler<T> handler)
```

- If inside a transaction (`autoCommit=false`), appends `FOR UPDATE` for pessimistic locking
- Logs execution duration in seconds
- Parameters are bound using type-specific `SqlStatementHandler` instances

**Mutation execution:**

```java
int executeMutation(
    Connection connection,
    String sqlString,
    Map<Integer, Object> parameter)
```

**Batch execution:**

```java
Map<String, Integer> executeMutationBatch(
    Connection connection,
    String sqlString,
    List<Map<Integer, Object>> parameterMapList)
```

Returns a map with keys: `"success"`, `"success.but.no.info"`, `"failed"`.

**Stored procedures:**

```java
// Oracle
<T> List<T> executeOracleStoreProcedure(
    Connection connection,
    String spName,
    Map<Integer, Object> inParameters,
    Map<Integer, OracleTypeEnum> outParameters,
    CallableStatementHandler<T> handler)

// PostgreSQL
<T> List<T> executePostgresStoreProcedure(
    Connection connection,
    String spName,
    Map<Integer, Object> inParameters,
    Map<Integer, PostgresTypeEnum> outParameters,
    CallableStatementHandler<T> handler)
```

### Type-Specific Handlers

Parameter binding uses `SqlStatementHandler` implementations:

| Java Type         | Handler                |
|-------------------|------------------------|
| `String`          | `StringHandler`        |
| `Integer`         | `IntegerHandler`       |
| `Long`            | `LongHandler`          |
| `Double`          | `DoubleHandler`        |
| `Float`           | `FloatHandler`         |
| `Boolean`         | `BooleanHandler`       |
| `BigDecimal`      | `BigDecimalHandler`    |
| `Date`            | `DateHandler`          |
| `LocalDate`       | `LocalDateHandler`     |
| `LocalDateTime`   | `LocalDateTimeHandler` |
| `Clob`            | `ClobHandler`          |

Result extraction uses `ResultSetHandler<T>` (maps a `ResultSet` row to an entity) and
`OutputSqlStatementHandler<T>` (extracts stored procedure OUT parameters).

---

## Entity Annotations

### @TableName (TYPE, RUNTIME)

Maps a class to a database table.

| Attribute  | Type       | Default | Description                |
|------------|------------|---------|----------------------------|
| `value`    | `String`   | —       | Table name (required)      |
| `schema`   | `String`   | `""`    | Schema prefix              |
| `indexes`  | `Index[]`  | `{}`    | Table-level index defs     |

### @ColumnName (FIELD, RUNTIME)

Maps a field to a database column.

| Attribute                   | Type      | Default | Description                     |
|-----------------------------|-----------|---------|----------------------------------|
| `name`                      | `String`  | `""`    | Column name                      |
| `nullable`                  | `boolean` | `true`  | NULL constraint                  |
| `defaultValue`              | `String`  | `""`    | Default value                    |
| `unique`                    | `boolean` | `false` | Unique constraint                |
| `index`                     | `boolean` | `false` | Create index                     |
| `columnDataTypeDefinition`  | `String`  | `""`    | Explicit SQL data type           |
| `insertable`                | `boolean` | `true`  | Include in INSERT                |
| `updatable`                 | `boolean` | `true`  | Include in UPDATE                |

### @IdColumn (FIELD, RUNTIME)

Marks the primary key field. Exactly one per entity.

### @Index

Used within `@TableName(indexes = ...)` for composite indexes.

| Attribute  | Type       | Default | Description                 |
|------------|------------|---------|-----------------------------|
| `name`     | `String`   | —       | Index name                  |
| `columns`  | `String[]` | —       | Column names                |
| `unique`   | `boolean`  | `false` | Whether the index is unique |

### @ForeignKey (FIELD, RUNTIME)

Defines a foreign key relationship.

| Attribute         | Type      | Default | Description           |
|-------------------|-----------|---------|-----------------------|
| `referenceColumn` | `String`  | —       | Target column name    |
| `referenceTable`  | `String`  | —       | Target table name     |
| `cascade`         | `boolean` | `false` | Cascade delete/update |

### @Clob (FIELD, RUNTIME)

Marks a field as a Character Large Object column.

### Example Entity

```java
@TableName(
    value = "tasks",
    schema = "public",
    indexes = @Index(name = "idx_tasks_name", columns = {"task_name"})
)
public class TaskEntity extends BaseEntity {

    @IdColumn
    @ColumnName(name = "TASK_ID", columnDataTypeDefinition = "BIGINT")
    private Long taskId;

    @ColumnName(name = "TASK_NAME", nullable = false)
    private String taskName;

    @ColumnName(name = "TASK_DETAIL")
    private String taskDetail;

    @ColumnName(name = "STATUS", nullable = false, defaultValue = "'PENDING'")
    @ForeignKey(referenceTable = "task_statuses", referenceColumn = "STATUS_CODE")
    private String status;

    @Clob
    @ColumnName(name = "DESCRIPTION")
    private String description;
}
```

### BaseEntity

Abstract base class with audit fields:

| Field       | Column        | Behavior                          |
|-------------|---------------|-----------------------------------|
| `createdAt` | `CREATED_AT`  | Set on insert, not updatable      |
| `updatedAt` | `UPDATED_AT`  | Auto-updated on insert and update |
| `deletedAt` | `DELETED_AT`  | Soft delete timestamp             |
| `createdBy` | `CREATED_BY`  | Set on insert, not updatable      |
| `updatedBy` | `UPDATED_BY`  | Updated on each save              |

### BaseEntityDTO

DTO counterpart of `BaseEntity` with `LocalDateTime`-based audit fields:

| Field       | Type            | Description                    |
|-------------|-----------------|--------------------------------|
| `createdAt` | `LocalDateTime` | Record creation timestamp      |
| `updatedAt` | `LocalDateTime` | Last modification timestamp    |
| `deletedAt` | `LocalDateTime` | Soft delete timestamp          |
| `createdBy` | `String`        | User who created the record    |
| `updatedBy` | `String`        | User who last modified         |

### BaseUnixEntityDTO

DTO with Unix timestamps (`BigInteger`) for audit fields:

| Field       | Type            | Description                       |
|-------------|-----------------|-----------------------------------|
| `id`        | `Long`          | Primary key                       |
| `createdAt` | `BigInteger`    | Creation timestamp (Unix ms)      |
| `updatedAt` | `BigInteger`    | Update timestamp (Unix ms)        |
| `deletedAt` | `BigInteger`    | Soft delete timestamp (Unix ms)   |
| `createdBy` | `String`        | Creator user identifier           |
| `updatedBy` | `String`        | Modifier user identifier          |

---

## DDL Generation (Strategy Pattern)

### DatabaseStrategy Interface

Each database has its own strategy implementation for DDL generation:

```java
public interface DatabaseStrategy {
    String generateIdColumnDefinition(String tableName, String columnName, String dataType);
    String generateCreateIndex(String indexName, String tableName, String columnExpression, boolean isUnique);
    String generateDropIndex(String indexName, String tableName);
    String generateAddColumn(ColumnDefinition col, String tableName);
    String generateDropColumn(String columnName, String tableName);
    String generateModifyColumn(ColumnDefinition col, String tableName);
    String generateRenameColumn(String columnName, String tableName);
    String generateSequenceStatement(String tableName);
    String generateForeignKeyCascade(boolean cascade);
}
```

### Strategy Implementations

| Strategy             | ID Column                   | Sequence     | Cascade                                |
|----------------------|-----------------------------|--------------|----------------------------------------|
| `OracleStrategy`     | `NUMBER(18) DEFAULT seq.nextval NOT NULL PRIMARY KEY` | Yes | `ON DELETE CASCADE`          |
| `PostgreSQLStrategy` | `SERIAL PRIMARY KEY`        | No           | `ON DELETE SET NULL ON UPDATE CASCADE`  |
| `MySQLStrategy`      | `INT AUTO_INCREMENT PRIMARY KEY` | No      | `ON DELETE CASCADE ON UPDATE RESTRICT`  |
| `MSSQLStrategy`      | `INT IDENTITY(1,1) PRIMARY KEY`  | No      | `ON DELETE CASCADE ON UPDATE CASCADE`   |

### DatabaseStrategyFactory

```java
DatabaseStrategy strategy = DatabaseStrategyFactory.createStrategy("postgresql");
```

Supported values: `"oracle"`, `"postgresql"`, `"mysql"`, `"mssql"`.

### Entity Analysis Pipeline

```
EntityUtils.analyzeEntityClass(entityClass, databaseType, folderPath)
  ↓
EntityAnalysisContext  (parses @TableName, sorts fields, selects datatype map)
  ↓
EntityAnalyzer.analyze()  (iterates fields, delegates to FieldProcessor)
  ↓
FieldProcessor.processField()  (builds ColumnDefinition, generates DDL)
  ↓
SqlGenerator.generate()  (assembles final .sql file)
```

Output file: `<schema->tablename.sql` containing:

```sql
-- GENERATED AT 2026-01-15 10:30:00.000 BY LCX+LIB V2

-- ################# CREATE INDEX #################
CREATE INDEX TASK_NAME_INDEX ON public.tasks (TASK_NAME);

-- ################# ADD COLUMN #################
ALTER TABLE public.tasks ADD COLUMN TASK_NAME VARCHAR(255) NOT NULL;

-- ################# CREATE TABLE #################
CREATE TABLE public.tasks (
    TASK_ID SERIAL PRIMARY KEY,
    TASK_NAME VARCHAR(255) NOT NULL,
    ...
);

-- DROP TABLE public.tasks;
-- TRUNCATE TABLE public.tasks;

-- ################# FOREIGN KEY #################
ALTER TABLE public.tasks ADD CONSTRAINT FK_tasks_STATUS
    FOREIGN KEY (STATUS) REFERENCES task_statuses(STATUS_CODE)
    ON DELETE SET NULL ON UPDATE CASCADE;
```

---

## Java Type to SQL Data Type Mapping

| Java Type       | Oracle          | PostgreSQL         | MySQL           | MSSQL              |
|-----------------|-----------------|--------------------|-----------------|--------------------|
| `String`        | `VARCHAR2(255)` | `VARCHAR(255)`     | `VARCHAR(255)`  | `NVARCHAR(255)`    |
| `Boolean`       | `NUMBER(1)`     | `BOOLEAN`          | `BOOLEAN`       | `BIT`              |
| `Integer`       | `NUMBER(10)`    | `INTEGER`          | `INT`           | `INT`              |
| `Long`          | `NUMBER(19)`    | `BIGINT`           | `BIGINT`        | `BIGINT`           |
| `Float`         | `NUMBER(7,2)`   | `REAL`             | `FLOAT`         | `REAL`             |
| `Double`        | `NUMBER(15,4)`  | `DOUBLE PRECISION` | `DOUBLE`        | `FLOAT`            |
| `BigDecimal`    | `NUMBER(19,2)`  | `NUMERIC(19,2)`    | `DECIMAL(19,2)` | `DECIMAL(19,2)`    |
| `LocalDate`     | `DATE`          | `DATE`             | `DATE`          | `DATE`             |
| `LocalDateTime` | `TIMESTAMP`     | `TIMESTAMP`        | `DATETIME`      | `DATETIME2`        |
| `UUID`          | `CHAR(36)`      | `UUID`             | `CHAR(36)`      | `UNIQUEIDENTIFIER` |
| `Clob`          | `CLOB`          | `TEXT`             | `TEXT`          | `NTEXT`            |
| `Blob`          | `BLOB`          | `BYTEA`            | `BLOB`          | `VARBINARY(MAX)`   |

---

## Compile-Time Code Generation (@SQLMapping)

**Processor:** `vn.com.lcx.processor.SQLMappingProcessor`
**Triggers on:** `@SQLMapping`
**Generates:** `{ClassName}Utils` (static methods) and `{ClassName}MappingImpl` (instance methods)

The `SQLMappingProcessor` generates two classes per entity annotated with `@SQLMapping`:

### \<Entity\>Utils (static methods)

```java
// JDBC ResultSet → Entity
static TaskEntity resultSetMapping(ResultSet resultSet)

// Entity → INSERT/UPDATE/DELETE SQL
static String insertStatement(TaskEntity model)
static String updateStatement(TaskEntity model)
static String deleteStatement(TaskEntity model)

// JDBC parameter maps (1-indexed)
static Map<Integer, Object> insertJDBCParams(TaskEntity model)
static Map<Integer, Object> updateJDBCParams(TaskEntity model)
static Map<Integer, Object> deleteJDBCParams(TaskEntity model)

// Vert.x Row → Entity
static TaskEntity vertxRowMapping(Row row)

// Reactive SQL with placeholders
static String reactiveInsertStatement(TaskEntity model, String placeHolder)
static String reactiveUpdateStatement(TaskEntity model, String placeHolder)
static String reactiveDeleteStatement(TaskEntity model, String placeHolder)

// Vert.x Tuple parameters
static Tuple insertTupleParam(TaskEntity model)
static Tuple updateTupleParam(TaskEntity model)
static Tuple deleteTupleParam(TaskEntity model)

// Field name → Column name lookup
static String getColumnNameFromFieldName(String fieldName)
```

### \<Entity\>MappingImpl (instance methods)

Implements `EntityMapping<T>` with the same methods as `<Entity>Utils` but as instance
methods. Registered in `EntityMappingContainer` for runtime lookup.

### SQL Generation Rules

- **INSERT**: Only includes non-null fields
- **UPDATE**: Only includes non-null fields (except primary key) in SET clause
- **DELETE**: WHERE clause on primary key only
- Non-nullable fields that are null throw `NullPointerException`
- Enum fields are converted to their `name()` String
- `@Clob` fields use special reader handling
- `@PreInsert`/`@PreUpdate` methods are called before parameter extraction

### Reactive Placeholder Support

| Database    | Placeholder | Example                              |
|-------------|-------------|--------------------------------------|
| Standard    | `?`         | `INSERT INTO t (a) VALUES (?)`       |
| PostgreSQL  | `$N`        | `INSERT INTO t (a) VALUES ($1)`      |
| SQL Server  | `@pN`       | `INSERT INTO t (a) VALUES (@p1)`     |

MSSQL also generates `OUTPUT INSERTED.<idColumn>` for reactive insert statements.

---

## Service Proxy Generation (@Service)

**Processor:** `vn.com.lcx.processor.ServiceProcessor`
**Triggers on:** `@Service`
**Generates:** `{ClassName}Proxy` (e.g., `UserService` → `UserServiceProxy`)

For each `@Service`-annotated class, generates a proxy class that wraps every public non-final,
non-static, non-abstract method with JPA context management and transaction boundaries.

**Generated proxy behavior per method:**

```
1. Check if this is the root call (JpaContext is empty)
2. If root → open JpaContext
3. If @Transactional → set transaction open, isolation level, mode
4. Try:
     Call actual.methodName(args)
     If root → commit
5. Catch specific @Transactional(onRollback = {...}) exceptions:
     If root → rollback
     Re-throw
6. Catch other exceptions:
     Re-throw InternalServiceException directly
     Wrap others in RuntimeException
7. Finally:
     If root → commit, close, clearAll
```

**`@Transactional` attributes:**

| Attribute    | Type       | Description                                              |
|--------------|------------|----------------------------------------------------------|
| `isolation`  | `int`      | Transaction isolation level                              |
| `mode`       | `int`      | Transaction mode                                         |
| `onRollback` | `Class[]`  | Exception types that trigger rollback                    |

**Example:**

```java
@Service
public class UserService {

    @Transactional(onRollback = {InternalServiceException.class})
    public UserDTO createUser(CreateUserRequest req) {
        // Business logic — proxy handles transaction boundaries
    }
}
```

The DI container registers the proxy (`UserServiceProxy`) instead of the original class,
so all callers automatically get transaction management.

---

## Repository Proxy Generation (@Repository)

**Processor:** `vn.com.lcx.processor.RepositoryProcessor`
**Triggers on:** `@Repository` (interface only, must extend `JpaRepository<E, ID>`)
**Generates:** `{InterfaceName}Proxy` (e.g., `UserRepository` → `UserRepositoryProxy`)

Generates concrete implementations for blocking JPA repository interfaces.

**Built-in methods (auto-generated):**

- `findById(ID)` — uses Hibernate `createQuery("FROM Entity WHERE idField = ?")`, returns
  `Optional<E>`. The ID field is detected via `@Id` annotation on the entity.
- `save`, `update`, `delete`, `find`, `findOne` — inherited from `JpaRepository` base
  implementation

**Custom method support:**

Methods annotated with `@Query` get generated implementations:

```java
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    @Query("FROM TaskEntity t WHERE t.status = ?1 AND t.priority > ?2")
    List<TaskEntity> findByStatusAndPriority(@Param("status") String status,
                                              @Param("priority") int priority);

    @Query(value = "SELECT * FROM tasks WHERE category = ?1", isNative = true)
    @ResultSetMapping(name = "TaskEntityMapping")
    List<TaskEntity> findByCategory(String category);

    @Modifying
    @Query("UPDATE TaskEntity t SET t.status = ?2 WHERE t.id = ?1")
    int updateStatus(Long id, String status);

    @Query("FROM TaskEntity t WHERE t.userId = ?1")
    Page<TaskEntity> findByUser(Long userId, Pageable pageable);
}
```

**Generated code features:**

- **HQL queries** — `session.createQuery(hql, Entity.class)`
- **Native queries** — `session.createNativeQuery(sql, Entity.class)`
- **`@Modifying`** — uses `createMutationQuery` / `createNativeMutationQuery`, returns row count
- **`@Param`** — named parameter binding (`query.setParameter("name", value)`)
- **Positional binding** — `query.setParameter(1, value)` when `@Param` is not present
- **`Pageable`** — adds `setFirstResult(offset)` + `setMaxResults(pageSize)`, generates automatic
  count query by replacing `SELECT ... FROM` with `SELECT COUNT(1) FROM`
- **`@ResultSetMapping`** — uses named result set mapping for native queries
- **Timing** — logs SQL execution duration at TRACE level
- **Session management** — each method tries `JpaContext` first; if not available, uses
  `doWork()` with a new session from `EntityContainer`

---

## Reactive Repository Processors

### ReactiveRepositoryProcessor

**Processor:** `vn.com.lcx.processor.ReactiveRepositoryProcessor`
**Triggers on:** `@RRepository` (interface only, must extend `ReactiveRepository<E>`)
**Generates:** `{InterfaceName}Impl`

Generates implementations for Vert.x reactive SQL client repositories.

**Built-in CRUD methods (auto-generated):**

| Method      | Description                                                           |
|-------------|-----------------------------------------------------------------------|
| `save`      | INSERT using generated `{Entity}Utils.reactiveInsertStatement()`      |
| `update`    | UPDATE using generated `{Entity}Utils.reactiveUpdateStatement()`      |
| `delete`    | DELETE using generated `{Entity}Utils.reactiveDeleteStatement()`      |
| `saveAll`   | Batch INSERT for a list of entities                                   |
| `updateAll` | Batch UPDATE for a list of entities                                   |
| `deleteAll` | Batch DELETE for a list of entities                                   |

**Key generated code behaviors:**

- **Database-specific placeholders** — detects database type via
  `connection.databaseMetadata().productName()` and selects `?` (MySQL/Oracle), `$N`
  (PostgreSQL), or `@pN` (MSSQL)
- **`@ReadOnly` entities** — CRUD operations return no-op futures
  (`Future.succeededFuture(null)`)
- **`@Query` methods** — generates `preparedQuery(sql).execute(Tuple.of(params))` with row
  mapping via `{Entity}Utils.vertxRowMapping(row)`
- **Return type handling:**
  - `Future<List<T>>` → collects all rows
  - `Future<Optional<T>>` → returns `Optional.empty()` if 0 rows, throws
    `NonUniqueQueryResult` if >1 row
  - `Future<Long/Integer>` → returns `rowSet.rowCount()` for modifying queries, or extracts
    value from first row for SELECT queries
- **Pageable support** — appends `pageable.toSql()` to query, generates count query by extracting
  `FROM` clause
- **Execution timing** — logs SQL duration at DEBUG level

### HRRepositoryProcessor

**Processor:** `vn.com.lcx.processor.HRRepositoryProcessor`
**Triggers on:** `@HRRepository` (interface only, must extend `HReactiveRepository<E>`)
**Generates:** `{InterfaceName}Impl`

Generates implementations for Hibernate Reactive repositories using `Stage.Session`.

**Built-in CRUD methods:**

| Method           | Implementation                                                    |
|------------------|-------------------------------------------------------------------|
| `save(entity)`   | `session.merge(entity)` — handles both insert and update          |
| `save(list)`     | Sequential merge with `flush()` + `clear()` every 50 items       |
| `delete(entity)` | `session.remove(entity)`                                          |
| `delete(list)`   | Sequential remove with `flush()` + `clear()` every 50 items      |
| `find(handler)`  | JPA Criteria query with `CriteriaHandler` predicate               |
| `findOne(handler)` | JPA Criteria query, wraps with `Optional.ofNullable()`          |
| `find(handler, pageable)` | Criteria query + count query, returns `Page<T>`          |

**`@Query` method support:**

```java
@HRRepository
public interface TasksRepository extends HReactiveRepository<TasksEntity> {

    @Query("FROM TasksEntity t WHERE t.user = ?1 AND t.status = ?2")
    Future<List<TasksEntity>> findByUserAndStatus(
            Stage.Session session, UsersEntity user, String status);

    @Modifying
    @Query(value = "UPDATE tasks SET status = ?2 WHERE id = ?1", isNative = true)
    Future<Integer> updateStatus(Stage.Session session, Long id, String status);
}
```

- **Parameter binding** — uses `@Param` for named parameters, positional (`?1`, `?2`) otherwise
- **`@Modifying`** — uses `createMutationQuery` or `createNativeQuery`, returns
  `Future<Integer>` with affected row count
- **`@ResultSetMapping`** — supported for native queries
- **Pageable** — generates a data query with `setFirstResult/setMaxResults` followed by a
  `SELECT COUNT(*)` query, returns `Page<T>`
- All results are wrapped: `Future.fromCompletionStage(session.createQuery(...).getResultList())`

---

## Repository Pattern (JPA)

### JpaRepository\<E, ID\>

Generic repository interface using Hibernate ORM:

```java
public interface JpaRepository<E, ID> {
    // Single entity CRUD
    E save(E entity);
    E update(E entity);
    void delete(E entity);

    // Batch operations
    void save(List<E> entities);
    void update(List<E> entities);
    void delete(List<E> entities);

    // Queries
    Optional<E> findById(ID id);
    Optional<E> findOne(CriteriaHandler<E> criteria);
    List<E> find(CriteriaHandler<E> criteria);
    Page<E> find(CriteriaHandler<E> criteria, Pageable pageable);

    // Low-level JDBC access
    void findById(org.hibernate.jdbc.Work work);
}
```

### CriteriaHandler\<E\>

Functional interface for dynamic JPA Criteria queries:

```java
@FunctionalInterface
public interface CriteriaHandler<E> {
    Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<E> root);
}
```

Usage:

```java
List<TaskEntity> tasks = taskRepository.find(
    (cb, cq, root) -> cb.and(
        cb.equal(root.get("status"), "ACTIVE"),
        cb.greaterThan(root.get("createdAt"), startDate)
    )
);
```

### JPA Functional Interfaces

| Interface                  | Signature                                      | Description                         |
|----------------------------|-------------------------------------------------|-------------------------------------|
| `RowMapper<T>`             | `T map(ResultSet rs)`                          | Single-row mapping from ResultSet   |
| `BatchCallback`            | `void handle(List<Map<String, String>> batch)` | Batch of generic map results        |
| `ResultBatchCallback<T>`   | `void handle(List<T> batch)`                   | Batch of typed results              |

```java
// RowMapper usage
RowMapper<TaskEntity> mapper = rs -> {
    TaskEntity task = new TaskEntity();
    task.setId(rs.getLong("TASK_ID"));
    task.setName(rs.getString("TASK_NAME"));
    return task;
};
```

---

### EntityContainer

Static registry mapping entity classes to Hibernate `SessionFactory` instances:

```java
EntityContainer.addEntityManager(entityList, sessionFactory);
SessionFactory sf = EntityContainer.getEntityManager(TaskEntity.class);
```

---

## Pagination

### Pageable Interface

```java
Pageable pageable = Pageable.ofPageable(1, 20)
    .add("createdAt", Direction.DESC)
    .add("taskName", Direction.ASC);
```

| Method                            | Description                              |
|-----------------------------------|------------------------------------------|
| `ofPageable(int page, int size)`  | Create pageable (1-based page number)    |
| `add(String field, Direction dir)`| Add sort column (fluent)                 |
| `fieldToColumn()`                 | Convert field names to DB column names   |
| `toSql()`                         | Generate LIMIT/OFFSET SQL clause         |

### Database-Specific Pagination

| Class                | SQL Style                                          |
|----------------------|----------------------------------------------------|
| `PostgreSQLPageable` | `LIMIT {size} OFFSET {offset}`                     |
| `MySqlPageable`      | `LIMIT {offset}, {size}`                           |
| `OraclePageable`     | `OFFSET {offset} ROWS FETCH NEXT {size} ROWS ONLY` |
| `MSSQLPageable`      | `OFFSET {offset} ROWS FETCH NEXT {size} ROWS ONLY` |

### Page\<T\> Result

```java
Page<TaskDTO> result = repository.find(criteria, pageable);
// result.getContent()          → List<TaskDTO>
// result.getPageNumber()       → 1
// result.getPageSize()         → 20
// result.getTotalElements()    → 150
// result.getTotalPages()       → 8
// result.isFirstPage()         → true
// result.isLastPage()          → false
```

Transform page content:

```java
Page<TaskResponse> responsePage = Page.create(result, task -> new TaskResponse(task));
```

---

## Specification Pattern

Fluent query builder for dynamic WHERE clauses (JDBC-level):

```java
Specification spec = Specification.create(TaskEntity.class)
    .where("status", "ACTIVE")
    .and("priority").greaterThan("priority", 3)
    .or("assignee").isNull("assignee");

String sql = "SELECT * FROM tasks WHERE " + spec.getFinalSQL();
List<Object> params = spec.getParameters();
```

Available operations:

| Method                 | SQL                          |
|------------------------|------------------------------|
| `equal(field, value)`  | `field = ?`                  |
| `notEqual(field, val)` | `field != ?`                 |
| `in(field, list)`      | `field IN (?, ?, ...)`       |
| `like(field, value)`   | `field LIKE ?`               |
| `between(f, v1, v2)`   | `field BETWEEN ? AND ?`      |
| `lessThan(field, val)` | `field < ?`                  |
| `lessThanOrEqual()`    | `field <= ?`                 |
| `greaterThan()`        | `field > ?`                  |
| `greaterThanOrEqual()` | `field >= ?`                 |
| `isNull(field)`        | `field IS NULL`              |
| `isNotNull(field)`     | `field IS NOT NULL`          |

Conditions are combined with `and()` / `or()`.

---

## Reactive Database Support

The framework offers two reactive database access strategies:

1. **Hibernate Reactive** (`@HRRepository`) - Uses Hibernate Reactive Stage API with JPA
   Criteria queries. Higher-level abstraction with entity lifecycle management.
2. **Vert.x SQL Client** (`@RRepository`) - Uses Vert.x reactive SQL clients directly.
   Lower-level with raw SQL control.

Both generate implementation classes at compile time via annotation processors.

---

### Reactive Annotations

#### @HRRepository (SOURCE retention)

Marks an interface as a Hibernate Reactive repository. Must extend `HReactiveRepository<T>`.

```java
@HRRepository
public interface UsersRepository extends HReactiveRepository<UsersEntity> {
}
```

#### @RRepository (SOURCE retention)

Marks an interface as a Vert.x reactive SQL client repository. Must extend
`ReactiveRepository<T>`. All methods must return `Future<T>`.

```java
@RRepository
public interface TaskRepository extends ReactiveRepository<TaskEntity> {
}
```

#### @Query (SOURCE retention, METHOD target)

Defines a native or JPQL query on a repository method.

| Attribute  | Type      | Default | Description              |
|------------|-----------|---------|--------------------------|
| `value`    | `String`  | —       | SQL or JPQL query string |
| `isNative` | `boolean` | `true`  | Native SQL vs JPQL       |

Parameters use positional binding: `?1`, `?2`, etc.

```java
@HRRepository
public interface TasksRepository extends HReactiveRepository<TasksEntity> {

    @Query("from TasksEntity t where t.user = ?1 and t.id = ?2")
    Future<Optional<TasksEntity>> findTaskDetail(
            Stage.Session session,
            UsersEntity user,
            Long id);
}
```

---

### HReactiveRepository\<T\> (Hibernate Reactive)

Base interface for Hibernate Reactive repositories. All methods take a `Stage.Session`
parameter from Hibernate Reactive.

```java
public interface HReactiveRepository<T> {

    // Single entity CRUD
    Future<T> save(Stage.Session session, T entity);
    Future<Void> delete(Stage.Session session, T entity);

    // Batch operations (flushes every 50 items)
    Future<List<T>> save(Stage.Session session, List<T> entity);
    Future<Void> delete(Stage.Session session, List<T> entity);

    // Criteria queries
    Future<List<T>> find(Stage.Session session, CriteriaHandler<T> criteriaHandler);
    Future<Optional<T>> findOne(Stage.Session session, CriteriaHandler<T> handler);
    Future<Page<T>> find(Stage.Session session, CriteriaHandler<T> handler, Pageable pageable);
}
```

**Usage example:**

```java
@Component
@RequiredArgsConstructor
public class UsersService {
    private final Stage.SessionFactory sessionFactory;
    private final UsersRepository usersRepository;

    public Future<Void> createUser(CreateUserRequest request) {
        var cs = sessionFactory.withSession(session -> {
            CriteriaHandler<UsersEntity> handler = (cb, cq, root) ->
                cb.equal(root.get("username"), request.getUsername());

            return usersRepository.findOne(session, handler)
                .map(opt -> {
                    if (opt.isPresent()) {
                        throw new InternalServiceException(AppError.USER_EXISTED);
                    }
                    return CommonConstant.VOID;
                })
                .compose(v -> {
                    UsersEntity user = new UsersEntity();
                    user.setUsername(request.getUsername());
                    user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
                    return usersRepository.save(session, user)
                        .compose(saved -> Future.fromCompletionStage(session.flush()));
                })
                .toCompletionStage();
        });
        return Future.fromCompletionStage(cs);
    }
}
```

**Generated code behavior:**
- `save()` uses `Session.merge()` — handles both insert and update
- Batch operations flush and clear every 50 items to prevent memory issues
- `find()` builds JPA Criteria queries from `CriteriaHandler`
- `findOne()` wraps result with `Optional.ofNullable()`
- Paginated `find()` executes a data query and a count query

---

### ReactiveRepository\<T\> (Vert.x SQL Client)

Base interface for Vert.x reactive SQL client repositories. All methods take
`RoutingContext context` and `SqlConnection connection`.

```java
public interface ReactiveRepository<T> {

    // CRUD
    Future<T> save(RoutingContext context, SqlConnection connection, T entity);
    Future<Integer> update(RoutingContext context, SqlConnection connection, T entity);
    Future<Integer> delete(RoutingContext context, SqlConnection connection, T entity);

    // Batch
    Future<List<T>> saveAll(RoutingContext context, SqlConnection connection, List<T> entities);
    Future<Integer> updateAll(RoutingContext context, SqlConnection connection, List<T> entities);
    Future<Integer> deleteAll(RoutingContext context, SqlConnection connection, List<T> entities);

    // Query (default methods)
    default <U> Future<Page<U>> find(RoutingContext context, SqlConnection connection,
            SqlStatement statement, ArrayList<Object> parameters,
            Pageable pageable, Class<U> outputClazz);

    default <U> Future<List<U>> find(RoutingContext context, SqlConnection connection,
            SqlStatement statement, ArrayList<Object> parameters, Class<U> outputClazz);

    default <U> Future<Optional<U>> findOne(RoutingContext context, SqlConnection connection,
            SqlStatement statement, ArrayList<Object> parameters, Class<U> outputClazz);

    default <U> Future<Optional<U>> findFirst(RoutingContext context, SqlConnection connection,
            SqlStatement statement, ArrayList<Object> parameters, Class<U> outputClazz);
}
```

**Key differences from HReactiveRepository:**
- Uses raw SQL statements instead of JPA Criteria
- `findOne()` throws `NonUniqueQueryResult` if more than one row
- `findFirst()` returns only the first row without uniqueness check
- Entity mapping uses `EntityMappingContainer` for row conversion
- Database-specific placeholder detection is automatic

---

### EntityMapping\<T\>

Bridges JDBC and Vert.x SQL client APIs for a single entity type:

```java
public interface EntityMapping<T> {
    // JDBC
    T resultSetMapping(ResultSet resultSet);
    Map<Integer, Object> insertJDBCParams(T model);
    Map<Integer, Object> updateJDBCParams(T model);
    Map<Integer, Object> deleteJDBCParams(T model);
    String insertStatement(T model);
    String updateStatement(T model);
    String deleteStatement(T model);

    // Vert.x Reactive
    T vertxRowMapping(Row row);
    Tuple insertTupleParam(T model);
    Tuple updateTupleParam(T model);
    Tuple deleteTupleParam(T model);
    String reactiveInsertStatement(T model, String placeHolder);
    String reactiveUpdateStatement(T model, String placeHolder);
    String reactiveDeleteStatement(T model, String placeHolder);

    // Field mapping
    String getColumnNameFromFieldName(String fieldName);
}
```

### EntityMappingContainer

Static registry for reactive entity mappings:

```java
// Registration (done automatically by ClassPool during scan)
EntityMappingContainer.addMapping("com.example.TaskEntity", new TaskEntityMappingImpl());

// Lookup
EntityMapping<TaskEntity> mapping = EntityMappingContainer.getMapping("com.example.TaskEntity");
TaskEntity entity = mapping.vertxRowMapping(row);
```

---

### SqlStatement - Fluent SQL Builder

Builds SELECT and COUNT queries for `ReactiveRepository` usage:

```java
SqlStatement stmt = SqlStatement.init()
    .select("t.id", "t.name", "t.status")
    .from("tasks t")
    .where("t.deleted_at IS NULL")
    .and("t.status = #")
    .and("t.name LIKE #")
    .order("t.created_at DESC");

ArrayList<Object> params = new ArrayList<>(List.of("ACTIVE", "%search%"));

// For paginated query
String sql = stmt.finalizeQueryStatement(pageable);  // SELECT ... LIMIT ... OFFSET ...
String countSql = stmt.finalizeCountStatement();      // SELECT COUNT(1) FROM ...
```

- Use `#` as placeholder (converted to `?`, `$1`, or `@p1` at runtime)
- Supports optional L1 caching via `SqlStatement.initWithCache("key")`
- `where()` must be called before `and()` / `or()`
- ORDER BY only applies to SELECT, not COUNT

---

### Transaction Management

`TransactionUtils` provides reactive transaction boundaries:

```java
Future<TaskEntity> result = TransactionUtils.executeInTransaction(pool, connection -> {
    return taskRepository.save(context, connection, entity)
        .compose(saved -> taskRepository.update(context, connection, anotherEntity))
        .map(v -> saved);
});
```

**Flow:**
1. Get connection from pool
2. Begin transaction
3. Execute function within transaction context
4. On success → commit
5. On failure → rollback (exception propagated, rollback exception suppressed)
6. Always close connection

---

### Reactive Configuration

#### ReactiveHibernateConfiguration (@Component)

Creates `Stage.SessionFactory` for Hibernate Reactive:

```java
@Instance
public Stage.SessionFactory sessionFactory() { ... }
```

Properties:
- `server.hreactive.database.host/port/name/username/password/type/max_pool_size`

Uses `ReactivePersistenceProvider` and registers entity classes from `ClassPool.getEntities()`.

#### ReactiveDbClientConfiguration (@Component)

Creates Vert.x SQL client `Pool` for direct reactive access:

```java
@Instance
public Pool pool() { ... }
```

Properties:
- `server.reactive.database.host/port/name/username/password/type/max_pool_size`

Supports PostgreSQL (`PgBuilder`), MySQL (`MySQLBuilder`), MSSQL (`MSSQLBuilder`),
Oracle (`OracleBuilder`), and generic JDBC (`JDBCPool`).

Pool options: `idleTimeout=30s`, configurable `maxSize`.

---

### Database Placeholder Detection

The framework automatically detects the database type from the connection and selects the
correct parameter placeholder:

| Database        | Placeholder | Example                      |
|-----------------|-------------|------------------------------|
| PostgreSQL      | `$N`        | `WHERE id = $1 AND name = $2` |
| SQL Server      | `@pN`       | `WHERE id = @p1 AND name = @p2` |
| MySQL / Oracle  | `?`         | `WHERE id = ? AND name = ?`  |

Detection uses `connection.databaseMetadata().productName()`.

---

### Hibernate Reactive Entities

Entities used with `@HRRepository` use standard JPA annotations (`@Entity`, `@Table`,
`@Column`, `@Id`, `@GeneratedValue`, etc.):

```java
@Entity
@Table(name = "tasks", schema = "todo")
public class TasksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_seq")
    @SequenceGenerator(name = "tasks_seq", sequenceName = "tasks_seq",
                       schema = "todo", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = DateTimeUtils.generateCurrentTimeDefault();
    }
}
```

---

### RowBatchCallback\<T, U\> (Reactive Batch Processing)

Async callback for reactive stream processing with stateful result chaining:

```java
public interface RowBatchCallback<T, U> {
    Future<U> handle(List<T> batch, U previousResult);
}
```

`previousResult` enables accumulating state across batch iterations.

---

### ReactiveRowStreamingUtils

Cursor-based batch streaming for large datasets using Vert.x SQL client:

```java
ReactiveRowStreamingUtils.stream(
    ctx, pool,
    sqlStatement,
    parameters,
    500,                    // batch size
    TaskEntity.class,
    (batch, previousResult) -> {
        // process batch of 500 rows
        return Future.succeededFuture(accumulatedResult);
    }
);
```

**How it works:**
1. Prepares SQL query with parameters
2. Creates cursor from prepared query
3. Fetches rows in configured batch sizes
4. Invokes callback with mapped batch + previous result
5. Recursively processes until cursor exhausted

---

### Reactive FileUtils

Non-blocking file I/O using Vert.x FileSystem API. Located at
`vn.com.lcx.reactive.utils.FileUtils` (distinct from the sync `vn.com.lcx.common.utils.FileUtils`).

All methods return `Future<T>` for async composition.

**Key methods (25+):**

| Category | Methods |
|----------|---------|
| Create   | `createFile()`, `createEmptyFile()`, `createDirectory()` |
| Read     | `readFileAsString()`, `readFileAsBuffer()` |
| Write    | `appendToFile()`, `overwriteFile()` |
| Delete   | `deleteFile()`, `deleteDirectory()` (recursive) |
| Navigate | `move()`, `copyFile()`, `exists()`, `getFileProperties()`, `listDirectory()` |
| Advanced | `openFile()`, `processFileLineByLine()`, `createSymbolicLink()`, `getRealPath()` |

---

### PreparedQueryWrapper\<T\>

Decorator wrapping Vert.x `PreparedQuery` to log query parameters at TRACE level:

- Parameter extraction with type-safe enum name conversion
- Truncates values > 1000 chars to first 10 + "..." + last 10 chars
- Supports `List<?>` values (flattened into multiple parameters)
- Handles null values safely

---

### PoolLcxWrapper

Decorator wrapping Vert.x SQL `Pool` with execution time measurement and `RoutingContext`
integration:

```java
PoolLcxWrapper pool = new PoolLcxWrapper(vertxPool);

// Get connection with timing
pool.getConnection(ctx).onSuccess(conn -> { ... });

// Execute within transaction with timing
pool.withTransaction(ctx, conn -> {
    return taskRepo.save(ctx, conn, entity);
});
```

---

### SqlConnectionLcxWrapper

Decorator for `SqlConnection` with SQL logging:

```java
SqlConnectionLcxWrapper.init(connection, context)
    .preparedQuery("SELECT * FROM tasks WHERE id = $1")
    .execute(Tuple.of(taskId))
    .map(rowSet -> { ... });
```

Logs all SQL statements at DEBUG level with execution timing.

---

## Key Source Files

| File                                      | Description                                |
|-------------------------------------------|--------------------------------------------|
| `common/database/DatabaseExecutor.java`   | JDBC execution interface                   |
| `common/database/DatabaseExecutorImpl.java` | JDBC execution implementation            |
| `common/database/DatabaseProperty.java`   | Connection configuration                   |
| `common/database/type/DBTypeEnum.java`    | Database type definitions                  |
| `common/database/DatabaseStrategy.java`   | DDL generation interface                   |
| `common/database/OracleStrategy.java`     | Oracle DDL strategy                        |
| `common/database/PostgreSQLStrategy.java` | PostgreSQL DDL strategy                    |
| `common/database/MySQLStrategy.java`      | MySQL DDL strategy                         |
| `common/database/MSSQLStrategy.java`      | SQL Server DDL strategy                    |
| `common/database/reflect/EntityAnalyzer.java` | Entity analysis orchestrator           |
| `common/database/reflect/FieldProcessor.java` | Field-level DDL processing             |
| `common/database/reflect/SqlGenerator.java`   | SQL file generation                    |
| `common/database/pageable/Pageable.java`  | Pagination interface                       |
| `common/database/pageable/Page.java`      | Page result wrapper                        |
| `common/database/specification/Specification.java` | Fluent query builder              |
| `common/database/handler/resultset/ResultSetHandler.java` | Row mapper interface       |
| `common/database/handler/statement/*.java` | Type-specific parameter handlers          |
| `common/annotation/mapper/TableName.java` | `@TableName` annotation                    |
| `common/annotation/mapper/ColumnName.java`| `@ColumnName` annotation                   |
| `common/annotation/mapper/IdColumn.java`  | `@IdColumn` annotation                     |
| `common/annotation/mapper/ForeignKey.java`| `@ForeignKey` annotation                   |
| `jpa/repository/JpaRepository.java`       | JPA repository interface                   |
| `reactive/repository/HReactiveRepository.java` | Hibernate Reactive repository interface |
| `reactive/repository/ReactiveRepository.java`  | Vert.x SQL client repository interface |
| `reactive/annotation/HRRepository.java`   | Hibernate Reactive repo annotation         |
| `reactive/annotation/RRepository.java`    | Vert.x reactive repo annotation            |
| `reactive/annotation/Query.java`          | Native/JPQL query annotation               |
| `reactive/entity/EntityMapping.java`      | Entity mapping interface (JDBC + reactive) |
| `reactive/context/EntityMappingContainer.java` | Entity mapping registry               |
| `reactive/helper/SqlStatement.java`       | Fluent SQL builder                         |
| `reactive/utils/TransactionUtils.java`    | Reactive transaction management            |
| `reactive/config/ReactiveHibernateConfiguration.java` | SessionFactory config        |
| `reactive/config/ReactiveDbClientConfiguration.java`  | Vert.x Pool config           |
| `reactive/wrapper/SqlConnectionLcxWrapper.java` | SQL connection with logging          |
| `processor/HRRepositoryProcessor.java`    | Hibernate Reactive repo code generator     |
| `processor/ReactiveRepositoryProcessor.java` | Vert.x reactive repo code generator    |
| `processor/SQLMappingProcessor.java`      | Entity utils code generator                |
| `jpa/dto/BaseEntityDTO.java`             | DTO with LocalDateTime audit fields         |
| `jpa/dto/BaseUnixEntityDTO.java`         | DTO with Unix timestamp audit fields        |
| `jpa/functional/RowMapper.java`          | Single-row ResultSet mapping interface      |
| `jpa/functional/BatchCallback.java`      | Batch processing callback                   |
| `jpa/functional/ResultBatchCallback.java`| Typed batch processing callback             |
| `reactive/functional/RowBatchCallback.java` | Reactive stateful batch callback         |
| `reactive/utils/ReactiveRowStreamingUtils.java` | Cursor-based row streaming           |
| `reactive/utils/FileUtils.java`          | Non-blocking file I/O                       |
| `reactive/wrapper/PreparedQueryWrapper.java` | Query parameter logging decorator       |
| `reactive/wrapper/PoolLcxWrapper.java`   | Pool with timing and context integration    |
