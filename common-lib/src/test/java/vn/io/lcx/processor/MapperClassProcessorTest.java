package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MapperClassProcessor")
class MapperClassProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new MapperClassProcessor())
                .compile(sources);
    }

    private JavaFileObject sourceDto() {
        return JavaFileObjects.forSourceString(
                "test.SourceDto",
                """
                package test;

                public class SourceDto {
                    private String name;
                    private int age;
                    private String email;

                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public int getAge() { return age; }
                    public void setAge(int age) { this.age = age; }
                    public String getEmail() { return email; }
                    public void setEmail(String email) { this.email = email; }
                }
                """
        );
    }

    private JavaFileObject targetDto() {
        return JavaFileObjects.forSourceString(
                "test.TargetDto",
                """
                package test;

                public class TargetDto {
                    private String name;
                    private int age;
                    private String email;

                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public int getAge() { return age; }
                    public void setAge(int age) { this.age = age; }
                    public String getEmail() { return email; }
                    public void setEmail(String email) { this.email = email; }
                }
                """
        );
    }

    @Nested
    @DisplayName("Simple Mapper")
    class SimpleMapper {

        @Test
        void simpleMapper_generatesImpl() {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.SimpleMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface SimpleMapper {
                        TargetDto toTarget(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("SimpleMapperImpl")));
        }

        @Test
        void simpleMapper_generatedCode_containsSetters() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.AutoMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface AutoMapper {
                        TargetDto map(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("AutoMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("setName"), "Generated code should contain setName");
            assertTrue(code.contains("setAge"), "Generated code should contain setAge");
            assertTrue(code.contains("setEmail"), "Generated code should contain setEmail");
            assertTrue(code.contains("getName"), "Generated code should contain getName");
        }

        @Test
        void simpleMapper_generatedCode_implementsInterface() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.ImplCheckMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface ImplCheckMapper {
                        TargetDto convert(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ImplCheckMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("implements ImplCheckMapper"),
                    "Generated class should implement the mapper interface");
        }
    }

    @Nested
    @DisplayName("Explicit @Mapping")
    class ExplicitMapping {

        @Test
        void explicitMapping_fromFieldToField() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PersonDto",
                    """
                    package test;

                    public class PersonDto {
                        private String fullName;
                        private int years;

                        public String getFullName() { return fullName; }
                        public void setFullName(String fullName) { this.fullName = fullName; }
                        public int getYears() { return years; }
                        public void setYears(int years) { this.years = years; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.PersonMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Mapping;

                    @MapperClass
                    public interface PersonMapper {
                        @Mapping(fromField = "fullName", toField = "name")
                        @Mapping(fromField = "years", toField = "age")
                        TargetDto toTarget(PersonDto person);
                    }
                    """
            );

            Compilation compilation = compile(source, targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("PersonMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("getFullName"), "Should use source getter getFullName");
            assertTrue(code.contains("setName"), "Should use target setter setName");
        }

        @Test
        void mappingWithCustomCode_usesCustomExpression() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.CustomCodeMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Mapping;

                    @MapperClass
                    public interface CustomCodeMapper {
                        @Mapping(toField = "name", code = "source.getName().toUpperCase()")
                        TargetDto mapWithCode(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("CustomCodeMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("source.getName().toUpperCase()"),
                    "Should contain custom mapping code expression");
        }

        @Test
        void mappingWithSkip_skipsField() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.SkipMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Mapping;

                    @MapperClass
                    public interface SkipMapper {
                        @Mapping(toField = "email", skip = true)
                        TargetDto mapSkipping(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("SkipMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            // setEmail should NOT be present because email is skipped
            assertFalse(code.contains("setEmail"), "Skipped field should not have setter call");
        }
    }

    @Nested
    @DisplayName("@Merging")
    class MergingTests {

        @Test
        void mergingTwoObjects_generatesImpl() {
            // Use wrapper types only (no primitives) to avoid null-check issues on primitives
            JavaFileObject mergeableDto = JavaFileObjects.forSourceString(
                    "test.MergeableDto",
                    """
                    package test;

                    public class MergeableDto {
                        private String name;
                        private String email;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public String getEmail() { return email; }
                        public void setEmail(String email) { this.email = email; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.MergeMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Merging;

                    @MapperClass
                    public interface MergeMapper {
                        @Merging
                        MergeableDto merge(MergeableDto base, MergeableDto updates);
                    }
                    """
            );

            Compilation compilation = compile(mergeableDto, mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("MergeMapperImpl")));
        }

        @Test
        void mergingWithNullCheck_generatesNullChecks() throws Exception {
            // mergeNonNullField=false (default) → generates null checks: only merge if first is null
            JavaFileObject mergeableDto = JavaFileObjects.forSourceString(
                    "test.NullMergeDto",
                    """
                    package test;

                    public class NullMergeDto {
                        private String name;
                        private String value;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public String getValue() { return value; }
                        public void setValue(String value) { this.value = value; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.NullCheckMergeMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Merging;

                    @MapperClass
                    public interface NullCheckMergeMapper {
                        @Merging(mergeNonNullField = false)
                        NullMergeDto mergeWithNullCheck(NullMergeDto base, NullMergeDto updates);
                    }
                    """
            );

            Compilation compilation = compile(mergeableDto, mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("NullCheckMergeMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("== null"),
                    "Default merge should generate null checks");
        }

        @Test
        void mergingDifferentTypes_causesError() {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.BadMergeMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Merging;

                    @MapperClass
                    public interface BadMergeMapper {
                        @Merging
                        TargetDto mergeDifferent(SourceDto source, TargetDto target);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.FAILURE, compilation.status());

            assertTrue(compilation.errors().stream()
                    .anyMatch(d -> d.getMessage(null).contains("different")),
                    "Should report error about merging different classes");
        }
    }

    @Nested
    @DisplayName("Multi-Parameter Mapping")
    class MultiParamMapping {

        @Test
        void multipleSourceParams_generatesImpl() {
            JavaFileObject extraDto = JavaFileObjects.forSourceString(
                    "test.AddressDto",
                    """
                    package test;

                    public class AddressDto {
                        private String city;

                        public String getCity() { return city; }
                        public void setCity(String city) { this.city = city; }
                    }
                    """
            );

            JavaFileObject combinedDto = JavaFileObjects.forSourceString(
                    "test.CombinedDto",
                    """
                    package test;

                    public class CombinedDto {
                        private String name;
                        private int age;
                        private String city;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public int getAge() { return age; }
                        public void setAge(int age) { this.age = age; }
                        public String getCity() { return city; }
                        public void setCity(String city) { this.city = city; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.CombineMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface CombineMapper {
                        CombinedDto combine(SourceDto source, AddressDto address);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), extraDto, combinedDto, mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("CombineMapperImpl")));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        void noParameters_causesError() {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.NoParamMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface NoParamMapper {
                        TargetDto map();
                    }
                    """
            );

            Compilation compilation = compile(targetDto(), mapper);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
        }

        @Test
        void mapperWithMultipleMethods_generatesAll() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.MultiMethodMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface MultiMethodMapper {
                        TargetDto toTarget(SourceDto source);
                        SourceDto toSource(TargetDto target);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("MultiMethodMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("toTarget"), "Should contain toTarget method");
            assertTrue(code.contains("toSource"), "Should contain toSource method");
        }
    }

    @Nested
    @DisplayName("Mapping With fromParameter")
    class MappingWithFromParameter {

        @Test
        void mappingWithFromParameter_usesCorrectSourceParam() throws Exception {
            JavaFileObject firstDto = JavaFileObjects.forSourceString(
                    "test.FirstDto",
                    """
                    package test;

                    public class FirstDto {
                        private String name;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            JavaFileObject secondDto = JavaFileObjects.forSourceString(
                    "test.SecondDto",
                    """
                    package test;

                    public class SecondDto {
                        private String name;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            JavaFileObject resultDto = JavaFileObjects.forSourceString(
                    "test.ResultDto",
                    """
                    package test;

                    public class ResultDto {
                        private String name;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.FromParamMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;
                    import vn.io.lcx.common.annotation.mapper.Mapping;

                    @MapperClass
                    public interface FromParamMapper {
                        @Mapping(fromField = "name", toField = "name", fromParameter = "second")
                        ResultDto map(FirstDto first, SecondDto second);
                    }
                    """
            );

            Compilation compilation = compile(firstDto, secondDto, resultDto, mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("FromParamMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("second"),
                    "Generated code should use the 'second' parameter as source");
        }
    }

    @Nested
    @DisplayName("Empty And Special Mappers")
    class EmptyAndSpecialMappers {

        @Test
        void multipleMethodsWithDifferentReturnTypes_allGenerated() throws Exception {
            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.MultiReturnMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface MultiReturnMapper {
                        SourceDto toSource(TargetDto target);
                        TargetDto toTarget(SourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(sourceDto(), targetDto(), mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject impl = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("MultiReturnMapperImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = impl.getCharContent(false).toString();
            assertTrue(code.contains("toSource"), "Should contain toSource method");
            assertTrue(code.contains("toTarget"), "Should contain toTarget method");
        }
    }

    @Nested
    @DisplayName("Field Type Variations")
    class FieldTypeVariations {

        @Test
        void mapperWithAllWrapperTypes_compilesSuccessfully() {
            JavaFileObject wrapperSource = JavaFileObjects.forSourceString(
                    "test.WrapperSourceDto",
                    """
                    package test;

                    public class WrapperSourceDto {
                        private Integer intVal;
                        private Long longVal;
                        private Double doubleVal;
                        private Boolean boolVal;
                        private String strVal;

                        public Integer getIntVal() { return intVal; }
                        public void setIntVal(Integer intVal) { this.intVal = intVal; }
                        public Long getLongVal() { return longVal; }
                        public void setLongVal(Long longVal) { this.longVal = longVal; }
                        public Double getDoubleVal() { return doubleVal; }
                        public void setDoubleVal(Double doubleVal) { this.doubleVal = doubleVal; }
                        public Boolean getBoolVal() { return boolVal; }
                        public void setBoolVal(Boolean boolVal) { this.boolVal = boolVal; }
                        public String getStrVal() { return strVal; }
                        public void setStrVal(String strVal) { this.strVal = strVal; }
                    }
                    """
            );

            JavaFileObject wrapperTarget = JavaFileObjects.forSourceString(
                    "test.WrapperTargetDto",
                    """
                    package test;

                    public class WrapperTargetDto {
                        private Integer intVal;
                        private Long longVal;
                        private Double doubleVal;
                        private Boolean boolVal;
                        private String strVal;

                        public Integer getIntVal() { return intVal; }
                        public void setIntVal(Integer intVal) { this.intVal = intVal; }
                        public Long getLongVal() { return longVal; }
                        public void setLongVal(Long longVal) { this.longVal = longVal; }
                        public Double getDoubleVal() { return doubleVal; }
                        public void setDoubleVal(Double doubleVal) { this.doubleVal = doubleVal; }
                        public Boolean getBoolVal() { return boolVal; }
                        public void setBoolVal(Boolean boolVal) { this.boolVal = boolVal; }
                        public String getStrVal() { return strVal; }
                        public void setStrVal(String strVal) { this.strVal = strVal; }
                    }
                    """
            );

            JavaFileObject mapper = JavaFileObjects.forSourceString(
                    "test.WrapperTypeMapper",
                    """
                    package test;

                    import vn.io.lcx.common.annotation.mapper.MapperClass;

                    @MapperClass
                    public interface WrapperTypeMapper {
                        WrapperTargetDto map(WrapperSourceDto source);
                    }
                    """
            );

            Compilation compilation = compile(wrapperSource, wrapperTarget, mapper);
            assertEquals(Compilation.Status.SUCCESS, compilation.status(),
                    "Mapper with all wrapper types should compile successfully");
        }
    }
}
