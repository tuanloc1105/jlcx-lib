package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SQLMappingProcessor")
class SQLMappingProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new SQLMappingProcessor())
                .compile(sources);
    }

    private String errors(Compilation compilation) {
        return compilation.errors().toString();
    }

    @Nested
    @DisplayName("Valid Entity")
    class ValidEntity {

        @Test
        void validEntity_generatesUtilsAndMappingImpl() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.UserEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("users")
                    public class UserEntity {
                        @IdColumn
                        private Long id;
                        private String name;
                        @ColumnName(name = "EMAIL_ADDRESS")
                        private String email;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public String getEmail() { return email; }
                        public void setEmail(String email) { this.email = email; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("UserEntityUtils")));
            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("UserEntityMappingImpl")));
        }

        @Test
        void validEntity_generatedUtils_containsExpectedMethods() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.SimpleEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("simple_table")
                    public class SimpleEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("SimpleEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("resultSetMapping"), "Should contain resultSetMapping method");
            assertTrue(generatedCode.contains("vertxRowMapping"), "Should contain vertxRowMapping method");
            assertTrue(generatedCode.contains("insertStatement"), "Should contain insertStatement method");
            assertTrue(generatedCode.contains("updateStatement"), "Should contain updateStatement method");
            assertTrue(generatedCode.contains("deleteStatement"), "Should contain deleteStatement method");
            assertTrue(generatedCode.contains("insertJDBCParams"), "Should contain insertJDBCParams method");
            assertTrue(generatedCode.contains("updateJDBCParams"), "Should contain updateJDBCParams method");
            assertTrue(generatedCode.contains("deleteJDBCParams"), "Should contain deleteJDBCParams method");
            assertTrue(generatedCode.contains("insertTupleParam"), "Should contain insertTupleParam method");
            assertTrue(generatedCode.contains("updateTupleParam"), "Should contain updateTupleParam method");
            assertTrue(generatedCode.contains("deleteTupleParam"), "Should contain deleteTupleParam method");
            assertTrue(generatedCode.contains("idColumnName"), "Should contain idColumnName method");
            assertTrue(generatedCode.contains("idRowExtract"), "Should contain idRowExtract method");
            assertTrue(generatedCode.contains("getColumnNameFromFieldName"), "Should contain getColumnNameFromFieldName method");
        }

        @Test
        void validEntity_generatedUtils_containsCorrectTableName() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ProductEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("products")
                    public class ProductEntity {
                        @IdColumn
                        private Long id;
                        private String productName;
                        private Double price;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getProductName() { return productName; }
                        public void setProductName(String productName) { this.productName = productName; }
                        public Double getPrice() { return price; }
                        public void setPrice(Double price) { this.price = price; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ProductEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("products"), "Generated code should reference table name 'products'");
        }

        @Test
        void tableNameWithSchema_generatesQualifiedTableName() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.SchemaEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName(value = "products", schema = "catalog")
                    public class SchemaEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("SchemaEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("catalog.products"));
        }
    }

    @Nested
    @DisplayName("SQL Projection")
    class SQLProjectionMapping {

        @Test
        void projectionWithoutTableNameOrId_generatesRowMappingUtilsOnly() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.DisputeProcessInfo",
                    """
                    package test;

                    import java.math.BigDecimal;
                    import vn.io.lcx.common.annotation.ColumnName;
                    import vn.io.lcx.common.annotation.SQLProjection;

                    @SQLProjection
                    public class DisputeProcessInfo {
                        @ColumnName(name = "ID")
                        private BigDecimal id;
                        @ColumnName(name = "PROCESS_ID")
                        private BigDecimal processId;
                        private String status;

                        public BigDecimal getId() { return id; }
                        public void setId(BigDecimal id) { this.id = id; }
                        public BigDecimal getProcessId() { return processId; }
                        public void setProcessId(BigDecimal processId) { this.processId = processId; }
                        public String getStatus() { return status; }
                        public void setStatus(String status) { this.status = status; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status(), errors(compilation));

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("DisputeProcessInfoUtils")));
            assertFalse(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("DisputeProcessInfoMappingImpl")));

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("DisputeProcessInfoUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("resultSetMapping"));
            assertTrue(generatedCode.contains("vertxRowMapping"));
            assertTrue(generatedCode.contains("row.getBigDecimal(\"PROCESS_ID\")"));
            assertTrue(generatedCode.contains("\"ID\""));
            assertFalse(generatedCode.contains("insertStatement"));
            assertFalse(generatedCode.contains("updateStatement"));
            assertFalse(generatedCode.contains("deleteStatement"));
            assertFalse(generatedCode.contains("idColumnName"));
        }

        @Test
        void projectionWithUnsupportedFieldType_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.UnsupportedProjection",
                    """
                    package test;

                    import java.util.Locale;
                    import vn.io.lcx.common.annotation.SQLProjection;

                    @SQLProjection
                    public class UnsupportedProjection {
                        private Locale locale;

                        public Locale getLocale() { return locale; }
                        public void setLocale(Locale locale) { this.locale = locale; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("Unsupported @SQLProjection field type"));
        }

        @Test
        void projectionWithoutGetter_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NoGetterProjection",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLProjection;

                    @SQLProjection
                    public class NoGetterProjection {
                        private String name;

                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("must have getter"));
        }

        @Test
        void classAnnotatedAsMappingAndProjection_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ConflictingModel",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.SQLProjection;
                    import vn.io.lcx.common.annotation.TableName;

                    @SQLMapping
                    @SQLProjection
                    @TableName("conflicting_model")
                    public class ConflictingModel {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("must not use both @SQLMapping and @SQLProjection"));
        }
    }

    @Nested
    @DisplayName("Missing Annotations")
    class MissingAnnotations {

        @Test
        void missingTableName_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NoTableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    public class NoTableEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("TableName"));
        }

        @Test
        void blankTableName_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.BlankTableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("")
                    public class BlankTableEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("must not be blank"));
        }

        @Test
        void missingIdColumn_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NoIdEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;

                    @SQLMapping
                    @TableName("no_id_table")
                    public class NoIdEntity {
                        private String name;
                        private String description;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public String getDescription() { return description; }
                        public void setDescription(String description) { this.description = description; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("primary key"));
        }

        @Test
        void multipleIdColumns_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.MultiIdEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("multi_id_table")
                    public class MultiIdEntity {
                        @IdColumn
                        private Long id1;
                        @IdColumn
                        private Long id2;
                        private String name;

                        public Long getId1() { return id1; }
                        public void setId1(Long id1) { this.id1 = id1; }
                        public Long getId2() { return id2; }
                        public void setId2(Long id2) { this.id2 = id2; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("More than one id column"));
        }
    }

    @Nested
    @DisplayName("Field Type Mapping")
    class FieldTypeMapping {

        @Test
        void allCommonFieldTypes_compileSuccessfully() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.AllTypesEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    import java.math.BigDecimal;
                    import java.time.LocalDate;
                    import java.time.LocalDateTime;
                    import java.time.OffsetDateTime;

                    @SQLMapping
                    @TableName("all_types")
                    public class AllTypesEntity {
                        @IdColumn
                        private Long id;
                        private String stringField;
                        private Integer intField;
                        private int primitiveIntField;
                        private Long longField;
                        private Double doubleField;
                        private Float floatField;
                        private Boolean booleanField;
                        private boolean primitiveBooleanField;
                        private BigDecimal bigDecimalField;
                        private LocalDate localDateField;
                        private LocalDateTime localDateTimeField;
                        @ColumnName(name = "OFFSET_DATE_TIME_FIELD")
                        private OffsetDateTime offsetDateTimeField;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getStringField() { return stringField; }
                        public void setStringField(String stringField) { this.stringField = stringField; }
                        public Integer getIntField() { return intField; }
                        public void setIntField(Integer intField) { this.intField = intField; }
                        public int getPrimitiveIntField() { return primitiveIntField; }
                        public void setPrimitiveIntField(int primitiveIntField) { this.primitiveIntField = primitiveIntField; }
                        public Long getLongField() { return longField; }
                        public void setLongField(Long longField) { this.longField = longField; }
                        public Double getDoubleField() { return doubleField; }
                        public void setDoubleField(Double doubleField) { this.doubleField = doubleField; }
                        public Float getFloatField() { return floatField; }
                        public void setFloatField(Float floatField) { this.floatField = floatField; }
                        public Boolean getBooleanField() { return booleanField; }
                        public void setBooleanField(Boolean booleanField) { this.booleanField = booleanField; }
                        public boolean isPrimitiveBooleanField() { return primitiveBooleanField; }
                        public void setPrimitiveBooleanField(boolean primitiveBooleanField) { this.primitiveBooleanField = primitiveBooleanField; }
                        public BigDecimal getBigDecimalField() { return bigDecimalField; }
                        public void setBigDecimalField(BigDecimal bigDecimalField) { this.bigDecimalField = bigDecimalField; }
                        public LocalDate getLocalDateField() { return localDateField; }
                        public void setLocalDateField(LocalDate localDateField) { this.localDateField = localDateField; }
                        public LocalDateTime getLocalDateTimeField() { return localDateTimeField; }
                        public void setLocalDateTimeField(LocalDateTime localDateTimeField) { this.localDateTimeField = localDateTimeField; }
                        public OffsetDateTime getOffsetDateTimeField() { return offsetDateTimeField; }
                        public void setOffsetDateTimeField(OffsetDateTime offsetDateTimeField) { this.offsetDateTimeField = offsetDateTimeField; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status(), errors(compilation));

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("AllTypesEntityUtils")));
            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("AllTypesEntityMappingImpl")));

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("AllTypesEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("resultSet.getObject(\"OFFSET_DATE_TIME_FIELD\", java.time.OffsetDateTime.class)"));
            assertTrue(generatedCode.contains("row.getOffsetDateTime(\"OFFSET_DATE_TIME_FIELD\")"));
            assertFalse(generatedCode.contains("Unknown type to generate code for field `offsetDateTimeField`"));
        }

        @Test
        void customColumnName_usedInGeneratedCode() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.CustomColEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("custom_col")
                    public class CustomColEntity {
                        @IdColumn
                        private Long id;
                        @ColumnName(name = "FULL_NAME")
                        private String fullName;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getFullName() { return fullName; }
                        public void setFullName(String fullName) { this.fullName = fullName; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("CustomColEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("FULL_NAME"), "Generated code should use custom column name FULL_NAME");
        }

        @Test
        void primitiveIdField_handledCorrectly() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PrimitiveIdEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("primitive_id")
                    public class PrimitiveIdEntity {
                        @IdColumn
                        private long id;
                        private String name;

                        public long getId() { return id; }
                        public void setId(long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());
        }

        @Test
        void primitiveBooleanIsPrefixedField_usesIsGetterAndSetWithoutIsPrefix() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.BooleanEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("boolean_entity")
                    public class BooleanEntity {
                        @IdColumn
                        private Long id;
                        private boolean isActive;
                        private boolean issue;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public boolean isActive() { return isActive; }
                        public void setActive(boolean active) { isActive = active; }
                        public boolean isIssue() { return issue; }
                        public void setIssue(boolean issue) { this.issue = issue; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("BooleanEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("model.isActive()"));
            assertTrue(generatedCode.contains("instance.setActive"));
            assertTrue(generatedCode.contains("model.isIssue()"));
            assertTrue(generatedCode.contains("instance.setIssue"));
        }

        @Test
        void fluentSetters_compileSuccessfully() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.FluentSetterEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("fluent_setter")
                    public class FluentSetterEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public FluentSetterEntity setId(Long id) { this.id = id; return this; }
                        public String getName() { return name; }
                        public FluentSetterEntity setName(String name) { this.name = name; return this; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());
        }

        @Test
        void staticAndFinalFields_areSkipped() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.StaticFinalEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("sf_table")
                    public class StaticFinalEntity {
                        @IdColumn
                        private Long id;
                        private static final String TABLE = "sf_table";
                        private static int counter = 0;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("StaticFinalEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertFalse(generatedCode.contains("setTABLE"), "Should not generate setter for static final field");
            assertFalse(generatedCode.contains("setCounter"), "Should not generate setter for static field");
        }

        @Test
        void nonInsertableField_notInInsertStatement() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NonInsertableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("non_insertable")
                    public class NonInsertableEntity {
                        @IdColumn
                        private Long id;
                        @ColumnName(name = "CREATED_AT", insertable = false)
                        private String createdAt;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getCreatedAt() { return createdAt; }
                        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());
        }
    }

    @Nested
    @DisplayName("MappingImpl Generation")
    class MappingImplGeneration {

        @Test
        void mappingImpl_hasNonStaticMethods() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ImplEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("impl_table")
                    public class ImplEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject mappingImpl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ImplEntityMappingImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = mappingImpl.getCharContent(false).toString();
            assertFalse(code.contains("public static"), "MappingImpl should not have static methods");
            assertTrue(code.contains("public "), "MappingImpl should have public methods");
        }
    }

    @Nested
    @DisplayName("Enum and Special Types")
    class EnumAndSpecialTypes {

        @Test
        void enumField_handledWithValueOf() throws Exception {
            JavaFileObject enumSource = JavaFileObjects.forSourceString(
                    "test.Status",
                    """
                    package test;

                    public enum Status {
                        ACTIVE, INACTIVE, PENDING
                    }
                    """
            );

            JavaFileObject entitySource = JavaFileObjects.forSourceString(
                    "test.EnumEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("enum_table")
                    public class EnumEntity {
                        @IdColumn
                        private Long id;
                        private Status status;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public Status getStatus() { return status; }
                        public void setStatus(Status status) { this.status = status; }
                    }
                    """
            );

            Compilation compilation = compile(enumSource, entitySource);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("EnumEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("valueOf"), "Generated code should use valueOf for enum field");
            assertTrue(generatedCode.contains("value != null"), "Generated code should map null enum values to null");
            assertTrue(generatedCode.contains("IllegalStateException"), "Generated mapping should fail fast on invalid enum values");
        }

        @Test
        void bigIntegerField_compileSuccessfully() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.BigIntEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import java.math.BigInteger;

                    @SQLMapping
                    @TableName("bigint_table")
                    public class BigIntEntity {
                        @IdColumn
                        private Long id;
                        private BigInteger bigValue;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public BigInteger getBigValue() { return bigValue; }
                        public void setBigValue(BigInteger bigValue) { this.bigValue = bigValue; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("BigIntEntityUtils")));
        }

        @Test
        void clobAnnotatedField_handledSpecially() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ClobEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.Clob;

                    @SQLMapping
                    @TableName("clob_table")
                    public class ClobEntity {
                        @IdColumn
                        private Long id;
                        @Clob
                        private String content;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getContent() { return content; }
                        public void setContent(String content) { this.content = content; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ClobEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("Clob") || generatedCode.contains("parseClobToString"),
                    "Generated code should handle Clob field specially");
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleCallbacks {

        @Test
        void preInsertMethod_calledInInsertStatement() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PreInsertEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreInsert;

                    @SQLMapping
                    @TableName("pre_insert_table")
                    public class PreInsertEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreInsert
                        public void beforeInsert() {
                            // lifecycle callback
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("PreInsertEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("beforeInsert"),
                    "Generated insertStatement code should call the @PreInsert method");
        }

        @Test
        void preUpdateMethod_calledInUpdateStatement() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PreUpdateEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreUpdate;

                    @SQLMapping
                    @TableName("pre_update_table")
                    public class PreUpdateEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreUpdate
                        public void beforeUpdate() {
                            // lifecycle callback
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("PreUpdateEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("beforeUpdate"),
                    "Generated updateStatement code should call the @PreUpdate method");
        }

        @Test
        void preInsertWithParameter_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.InvalidPreInsertParamEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreInsert;

                    @SQLMapping
                    @TableName("invalid_pre_insert_param")
                    public class InvalidPreInsertParamEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreInsert
                        public void beforeInsert(String value) {
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("@PreInsert method must not declare parameters"));
        }

        @Test
        void preInsertWithReturnValue_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.InvalidPreInsertReturnEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreInsert;

                    @SQLMapping
                    @TableName("invalid_pre_insert_return")
                    public class InvalidPreInsertReturnEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreInsert
                        public String beforeInsert() {
                            return name;
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("@PreInsert method must return void"));
        }

        @Test
        void preUpdateWithParameter_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.InvalidPreUpdateParamEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreUpdate;

                    @SQLMapping
                    @TableName("invalid_pre_update_param")
                    public class InvalidPreUpdateParamEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreUpdate
                        public void beforeUpdate(String value) {
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("@PreUpdate method must not declare parameters"));
        }

        @Test
        void preUpdateWithReturnValue_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.InvalidPreUpdateReturnEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.PreUpdate;

                    @SQLMapping
                    @TableName("invalid_pre_update_return")
                    public class InvalidPreUpdateReturnEntity {
                        @IdColumn
                        private Long id;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }

                        @PreUpdate
                        public String beforeUpdate() {
                            return name;
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("@PreUpdate method must return void"));
        }
    }

    @Nested
    @DisplayName("Column Name Attributes")
    class ColumnNameAttributes {

        @Test
        void nonUpdatableField_excludedFromUpdateStatement() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NonUpdatableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("non_updatable_table")
                    public class NonUpdatableEntity {
                        @IdColumn
                        private Long id;
                        @ColumnName(name = "CREATED_BY", updatable = false)
                        private String createdBy;
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getCreatedBy() { return createdBy; }
                        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("NonUpdatableEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();

            int updateIdx = generatedCode.indexOf("updateStatement");
            assertTrue(updateIdx >= 0, "Generated code should contain updateStatement method");

            String updateSection = generatedCode.substring(updateIdx,
                    generatedCode.indexOf("public", updateIdx + 1) > 0
                            ? generatedCode.indexOf("public", updateIdx + 1)
                            : generatedCode.length());
            assertFalse(updateSection.contains("CREATED_BY"),
                    "UPDATE code should not include non-updatable column CREATED_BY");
        }

        @Test
        void noInsertableColumns_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NoInsertableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("no_insertable")
                    public class NoInsertableEntity {
                        @IdColumn
                        @ColumnName(insertable = false)
                        private Long id;
                        @ColumnName(insertable = false)
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("at least one insertable column"));
        }

        @Test
        void noUpdatableNonIdColumns_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NoUpdatableEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;
                    import vn.io.lcx.common.annotation.ColumnName;

                    @SQLMapping
                    @TableName("no_updatable")
                    public class NoUpdatableEntity {
                        @IdColumn
                        private Long id;
                        @ColumnName(updatable = false)
                        private String name;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("at least one updatable non-id column"));
        }

        @Test
        void emptyEntityWithOnlyId_failsCompilation() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.IdOnlyEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    @SQLMapping
                    @TableName("id_only_table")
                    public class IdOnlyEntity {
                        @IdColumn
                        private Long id;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(errors(compilation).contains("at least one updatable non-id column"));
        }
    }
}
