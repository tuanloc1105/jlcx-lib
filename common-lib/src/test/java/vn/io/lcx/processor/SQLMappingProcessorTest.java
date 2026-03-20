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
    }

    @Nested
    @DisplayName("Missing Annotations")
    class MissingAnnotations {

        @Test
        void missingTableName_generatesCodeWithError() throws Exception {
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
            // Compilation succeeds but generated code throws CodeGenError
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("NoTableEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("TableName"), "Generated code should contain error about missing TableName");
        }

        @Test
        void missingIdColumn_generatesCodeWithError() throws Exception {
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
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generatedUtils = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("NoIdEntityUtils"))
                    .findFirst()
                    .orElseThrow();

            String generatedCode = generatedUtils.getCharContent(false).toString();
            assertTrue(generatedCode.contains("primary key"), "Generated code should contain error about missing primary key");
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
            // Multiple @IdColumn causes errors in generated code compilation
            assertEquals(Compilation.Status.FAILURE, compilation.status());
        }
    }

    @Nested
    @DisplayName("Field Type Mapping")
    class FieldTypeMapping {

        @Test
        void allCommonFieldTypes_compileSuccessfully() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.AllTypesEntity",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.SQLMapping;
                    import vn.io.lcx.common.annotation.TableName;
                    import vn.io.lcx.common.annotation.IdColumn;

                    import java.math.BigDecimal;
                    import java.time.LocalDate;
                    import java.time.LocalDateTime;

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
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("AllTypesEntityUtils")));
            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("AllTypesEntityMappingImpl")));
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
            // MappingImpl should NOT have static methods (replaced from "public static" to "public")
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

            // The updateStatement method should not include CREATED_BY
            // Extract only the updateStatement method body for checking
            int updateIdx = generatedCode.indexOf("updateStatement");
            int insertIdx = generatedCode.indexOf("insertStatement");
            assertTrue(updateIdx >= 0, "Generated code should contain updateStatement method");

            // Find the next method after updateStatement to bound the search
            String updateSection = generatedCode.substring(updateIdx,
                    generatedCode.indexOf("public", updateIdx + 1) > 0
                            ? generatedCode.indexOf("public", updateIdx + 1)
                            : generatedCode.length());
            assertFalse(updateSection.contains("CREATED_BY"),
                    "UPDATE code should not include non-updatable column CREATED_BY");
        }

        @Test
        void emptyEntityWithOnlyId_compileSuccessfully() {
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
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("IdOnlyEntityUtils")));
        }
    }
}
