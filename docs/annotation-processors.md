# Annotation Processors

## Overview

The `processor` module registers 9 annotation processors via
`META-INF/services/javax.annotation.processing.Processor`.

---

## MapperClassProcessor

**Processor:** `vn.io.lcx.processor.MapperClassProcessor`
**Triggers on:** `@MapperClass` (interface only)
**Generates:** `{InterfaceName}Impl`

Generates implementations for object mapper interfaces. Each method in the interface maps or
merges objects based on annotations and field name matching. The generated class is annotated
with `@Component` for automatic DI registration.

### Annotations

| Annotation      | Target | Description                                          |
|-----------------|--------|------------------------------------------------------|
| `@MapperClass`  | TYPE   | Marks an interface as a mapper (triggers processing) |
| `@MapperConfig` | TYPE   | Configuration for mapper code generation behavior    |
| `@Mapping`      | METHOD | Defines field-level mapping rules (repeatable)       |
| `@Merging`      | METHOD | Marks a method as a merge operation                  |

### `@Mapping` Attributes

| Attribute       | Type    | Default | Description                                                  |
|-----------------|---------|---------|--------------------------------------------------------------|
| `fromField`     | String  | `""`    | Source field name (defaults to same as `toField`)            |
| `toField`       | String  | `""`    | Target field name                                            |
| `code`          | String  | `""`    | Custom Java expression (used instead of getter)              |
| `skip`          | boolean | `false` | Skip this target field entirely                              |
| `nestedMapper`  | String  | `""`    | Mapper method name for nested object mapping                 |
| `nullSafe`      | boolean | `false` | Generates null-safe code for this mapping                    |
| `fromParameter` | String  | `""`    | Source parameter name (for multi-parameter mapping methods)  |

### `@Merging` Attributes

| Attribute         | Type    | Default | Description                                        |
|-------------------|---------|---------|----------------------------------------------------|
| `mergeNonNullField` | boolean | `false` | Only merge non-null fields from source into target |

### `@MapperConfig` Attributes

| Attribute                   | Type      | Default | Description                                              |
|-----------------------------|-----------|---------|----------------------------------------------------------|
| `nullSafeByDefault`         | `boolean` | `true`  | Generate null-safe code for all mappings                 |
| `addGeneratedAnnotation`    | `boolean` | `true`  | Add `@Generated` annotation with timestamp to output     |
| `componentName`             | `String`  | `""`    | Custom DI component name (empty uses generated name)     |
| `warnOnUnmappedTargetFields`| `boolean` | `false` | Compilation warning for unmapped fields                  |
| `strictTypeChecking`        | `boolean` | `false` | Fail compilation on type mismatches                      |

```java
@MapperClass
@MapperConfig(nullSafeByDefault = true, warnOnUnmappedTargetFields = true)
public interface TaskMapper {
    TaskDTO toDTO(TaskEntity entity);
}
```

---

### Single-Parameter Mapping

The simplest case: one source parameter, one return type. Fields are matched automatically
by name and type. Use `@Mapping` to override, skip, or customize individual fields.

```java
@MapperClass
public interface TaskMapper {

    @Mapping(fromField = "taskName", toField = "name")
    @Mapping(toField = "status", code = "\"ACTIVE\"")
    @Mapping(toField = "internalId", skip = true)
    TaskDTO toDTO(TaskEntity entity);
}
```

**Generated code:**

```java
@Generated(value = "vn.io.lcx.processor.MapperClassProcessor", date = "...")
@vn.io.lcx.common.annotation.Component
public class TaskMapperImpl implements TaskMapper {

    public TaskMapperImpl() {}

    @Override
    public TaskDTO toDTO(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        TaskDTO instance = new TaskDTO();
        instance.setName(entity.getTaskName());       // @Mapping(fromField="taskName", toField="name")
        instance.setStatus("ACTIVE");                  // @Mapping(toField="status", code="\"ACTIVE\"")
        // internalId skipped                          // @Mapping(toField="internalId", skip=true)
        instance.setPriority(entity.getPriority());    // auto-matched by name+type
        return instance;
    }
}
```

**Automatic field matching:** Fields not covered by explicit `@Mapping` annotations are
matched automatically by name and type. If both source and target have a field with the same
name and type, a `instance.setX(source.getX())` line is generated.

---

### Multi-Parameter Mapping

Mapping methods can accept multiple source parameters to combine fields from different
source objects into a single target object.

Use the `fromParameter` attribute in `@Mapping` to specify which parameter a field comes from.
The value must match the parameter name declared in the method signature.

```java
@MapperClass
public interface OrderMapper {

    @Mapping(fromParameter = "user", fromField = "firstName", toField = "customerName")
    @Mapping(fromParameter = "product", fromField = "productName", toField = "itemName")
    @Mapping(toField = "status", code = "\"PENDING\"")
    @Mapping(toField = "internalCode", skip = true)
    OrderDTO toOrderDTO(UserEntity user, ProductEntity product);
}
```

**Generated code:**

```java
@Override
public OrderDTO toOrderDTO(UserEntity user, ProductEntity product) {
    if (user == null || product == null) {
        return null;
    }
    OrderDTO instance = new OrderDTO();
    instance.setCustomerName(user.getFirstName());     // explicit: fromParameter="user"
    instance.setItemName(product.getProductName());    // explicit: fromParameter="product"
    instance.setStatus("PENDING");                      // custom code
    // internalCode skipped
    instance.setEmail(user.getEmail());                // auto-matched from user (first priority)
    instance.setPrice(product.getPrice());             // auto-matched from product
    return instance;
}
```

**Auto-matching rules for multiple parameters:**

- Target fields not covered by explicit `@Mapping` are auto-matched by name and type
  across all source parameters
- Parameters are checked in declaration order: the **first parameter** has highest priority
- When multiple parameters have a field with the same name and type, the first parameter wins
- If a `@Mapping` annotation omits `fromParameter`, it defaults to the first parameter

**Null handling:** If any source parameter is `null`, the method returns `null`.

---

### Object Merging

Use `@Merging` to merge two instances of the **same type**. Both parameters must have
identical types. Return type can be `void` or the entity type.

```java
@MapperClass
public interface TaskMapper {

    @Merging(mergeNonNullField = true)
    TaskEntity merge(TaskEntity target, TaskEntity source);
}
```

**Generated code:**

```java
@Override
public TaskEntity merge(TaskEntity target, TaskEntity source) {
    if (target == null || source == null) {
        return null;
    }
    if (target.getName() == null) {
        target.setName(source.getName());
    }
    if (target.getDescription() == null) {
        target.setDescription(source.getDescription());
    }
    return target;
}
```

When `mergeNonNullField = true`, only non-null fields from `source` overwrite `target`.
When `mergeNonNullField = false`, all fields from `source` are copied directly into `target`.

---

### Key Source Files

| File | Description |
|------|-------------|
| `processor/MapperClassProcessor.java` | Main annotation processor |
| `processor/template/CodeTemplates.java` | Code generation templates |
| `processor/service/FieldMappingResolver.java` | Field type matching and compatibility |
| `processor/service/MappingCodeGenerator.java` | Code snippet generation |
| `processor/utility/TypeHierarchyAnalyzer.java` | Type hierarchy traversal |
| `processor/model/FieldMappingInfo.java` | Field mapping metadata model |
| `processor/model/SourceParameterInfo.java` | Source parameter metadata model |
| `processor/exception/InvalidMappingException.java` | Mapping validation errors |
| `common/annotation/mapper/MapperClass.java` | `@MapperClass` annotation |
| `common/annotation/mapper/MapperConfig.java` | `@MapperConfig` annotation |
| `common/annotation/mapper/Mapping.java` | `@Mapping` annotation |
| `common/annotation/mapper/Merging.java` | `@Merging` annotation |

---

## Other Processors (Cross-References)

The remaining processors are documented in their respective domain docs:

| Processor                    | Annotation        | Documentation                                            |
|------------------------------|-------------------|----------------------------------------------------------|
| `SQLMappingProcessor`        | `@SQLMapping`     | [database-layer.md](database-layer.md)                   |
| `ServiceProcessor`           | `@Service`        | [database-layer.md](database-layer.md)                   |
| `RepositoryProcessor`        | `@Repository`     | [database-layer.md](database-layer.md)                   |
| `HRRepositoryProcessor`      | `@HRRepository`   | [database-layer.md](database-layer.md)                   |
| `ReactiveRepositoryProcessor`| `@RRepository`    | [database-layer.md](database-layer.md)                   |
| `ControllerProcessor`        | `@Controller`     | [vertx-web-framework.md](vertx-web-framework.md)        |
| `RestControllerProcessor`    | `@RestController` | [vertx-web-framework.md](vertx-web-framework.md)        |
| `DIScanner`                  | `@Component`      | [classpool-di-container.md](classpool-di-container.md)   |
