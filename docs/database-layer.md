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

| Attribute  | Type       | Description        |
|------------|------------|--------------------|
| `name`     | `String`   | Index name         |
| `columns`  | `String[]` | Column names       |

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

---

## DDL Generation (Strategy Pattern)

### DatabaseStrategy Interface

Each database has its own strategy implementation for DDL generation:

```java
public interface DatabaseStrategy {
    String generateIdColumnDefinition(String tableName, String columnName, String dataType);
    String generateCreateIndex(String columnName, String tableName, boolean isUnique);
    String generateDropIndex(String columnName, String tableName);
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

## Reactive Database Support (EntityMapping)

The `EntityMapping<T>` interface bridges JDBC and Vert.x SQL client APIs:

**JDBC methods:**
- `T resultSetMapping(ResultSet resultSet)`
- `Map<Integer, Object> insertJDBCParams(T model)`
- `String insertStatement(T model)`

**Vert.x Reactive methods:**
- `T vertxRowMapping(Row row)` - Map `io.vertx.sqlclient.Row` to entity
- `String reactiveInsertStatement(T model, String placeHolder)` - SQL with `$1` or `@p1`
- `Tuple insertTupleParam(T model)` - `io.vertx.sqlclient.Tuple` for binding

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
| `processor/SQLMappingProcessor.java`      | Entity utils code generator                |
