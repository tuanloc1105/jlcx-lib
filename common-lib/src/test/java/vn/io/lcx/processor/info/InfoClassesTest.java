package vn.io.lcx.processor.info;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the DI scanner info POJOs: {@link ClassInfo}, {@link FieldInfo},
 * {@link MethodInfo}, and {@link ConstructorInfo}.
 * <p>
 * These are simple data holders used by DIScanner to serialize class metadata
 * into META-INF/class-index-*.json files for the ClassPool DI container.
 */
class InfoClassesTest {

    // ================================================================== //
    //  FieldInfo
    // ================================================================== //

    @Nested
    class FieldInfoTest {

        @Test
        void noArgConstructorCreatesInstance() {
            FieldInfo info = new FieldInfo();

            assertNull(info.getFieldName());
            assertNull(info.getFieldDataType());
        }

        @Test
        void allArgsConstructorSetsValues() {
            FieldInfo info = new FieldInfo("userId", "java.lang.Long");

            assertEquals("userId", info.getFieldName());
            assertEquals("java.lang.Long", info.getFieldDataType());
        }

        @Test
        void setFieldNameUpdatesValue() {
            FieldInfo info = new FieldInfo();
            info.setFieldName("email");

            assertEquals("email", info.getFieldName());
        }

        @Test
        void setFieldDataTypeUpdatesValue() {
            FieldInfo info = new FieldInfo();
            info.setFieldDataType("java.lang.String");

            assertEquals("java.lang.String", info.getFieldDataType());
        }

        @Test
        void settersOverrideConstructorValues() {
            FieldInfo info = new FieldInfo("original", "int");
            info.setFieldName("updated");
            info.setFieldDataType("long");

            assertEquals("updated", info.getFieldName());
            assertEquals("long", info.getFieldDataType());
        }
    }

    // ================================================================== //
    //  MethodInfo (info package)
    // ================================================================== //

    @Nested
    class MethodInfoTest {

        @Test
        void noArgConstructorCreatesInstance() {
            MethodInfo info = new MethodInfo();

            assertNull(info.getMethodName());
            assertNull(info.getReturnDataType());
        }

        @Test
        void allArgsConstructorSetsValues() {
            MethodInfo info = new MethodInfo("init", "void");

            assertEquals("init", info.getMethodName());
            assertEquals("void", info.getReturnDataType());
        }

        @Test
        void setMethodNameUpdatesValue() {
            MethodInfo info = new MethodInfo();
            info.setMethodName("postConstruct");

            assertEquals("postConstruct", info.getMethodName());
        }

        @Test
        void setReturnDataTypeUpdatesValue() {
            MethodInfo info = new MethodInfo();
            info.setReturnDataType("io.vertx.core.Future");

            assertEquals("io.vertx.core.Future", info.getReturnDataType());
        }

        @Test
        void settersOverrideConstructorValues() {
            MethodInfo info = new MethodInfo("oldName", "String");
            info.setMethodName("newName");
            info.setReturnDataType("Integer");

            assertEquals("newName", info.getMethodName());
            assertEquals("Integer", info.getReturnDataType());
        }
    }

    // ================================================================== //
    //  ConstructorInfo
    // ================================================================== //

    @Nested
    class ConstructorInfoTest {

        @Test
        void noArgConstructorCreatesInstance() {
            ConstructorInfo info = new ConstructorInfo();

            assertNull(info.getParametersDataTypes());
        }

        @Test
        void allArgsConstructorSetsValues() {
            List<String> params = Arrays.asList("java.lang.String", "int");
            ConstructorInfo info = new ConstructorInfo(params);

            assertNotNull(info.getParametersDataTypes());
            assertEquals(2, info.getParametersDataTypes().size());
            assertEquals("java.lang.String", info.getParametersDataTypes().get(0));
            assertEquals("int", info.getParametersDataTypes().get(1));
        }

        @Test
        void emptyParameterList() {
            ConstructorInfo info = new ConstructorInfo(Collections.emptyList());

            assertNotNull(info.getParametersDataTypes());
            assertEquals(0, info.getParametersDataTypes().size());
        }

        @Test
        void setParametersDataTypesUpdatesValue() {
            ConstructorInfo info = new ConstructorInfo();
            info.setParametersDataTypes(Arrays.asList("long", "boolean"));

            assertEquals(2, info.getParametersDataTypes().size());
            assertEquals("long", info.getParametersDataTypes().get(0));
        }

        @Test
        void settersOverrideConstructorValues() {
            ConstructorInfo info = new ConstructorInfo(Arrays.asList("A", "B"));
            info.setParametersDataTypes(Collections.singletonList("C"));

            assertEquals(1, info.getParametersDataTypes().size());
            assertEquals("C", info.getParametersDataTypes().get(0));
        }
    }

    // ================================================================== //
    //  ClassInfo
    // ================================================================== //

    @Nested
    class ClassInfoTest {

        @Test
        void noArgConstructorCreatesInstance() {
            ClassInfo info = new ClassInfo();

            assertNull(info.getFullQualifiedClassName());
            assertNull(info.getSuperClassesFullName());
            assertNull(info.getFields());
            assertNull(info.getConstructor());
            assertNull(info.getPostConstruct());
            assertNull(info.getCreateInstanceMethods());
        }

        @Test
        void allArgsConstructorSetsAllValues() {
            List<String> supers = Arrays.asList("java.lang.Object", "vn.io.lcx.BaseService");
            List<FieldInfo> fields = Arrays.asList(
                    new FieldInfo("name", "String"),
                    new FieldInfo("age", "int")
            );
            ConstructorInfo ctor = new ConstructorInfo(Collections.singletonList("String"));
            MethodInfo postConstruct = new MethodInfo("init", "void");
            List<MethodInfo> createMethods = Collections.singletonList(
                    new MethodInfo("createInstance", "MyService")
            );

            ClassInfo info = new ClassInfo(
                    "vn.io.lcx.MyService",
                    supers, fields, ctor, postConstruct, createMethods
            );

            assertEquals("vn.io.lcx.MyService", info.getFullQualifiedClassName());
            assertEquals(2, info.getSuperClassesFullName().size());
            assertEquals(2, info.getFields().size());
            assertNotNull(info.getConstructor());
            assertNotNull(info.getPostConstruct());
            assertEquals("init", info.getPostConstruct().getMethodName());
            assertEquals(1, info.getCreateInstanceMethods().size());
        }

        @Test
        void setFullQualifiedClassNameUpdatesValue() {
            ClassInfo info = new ClassInfo();
            info.setFullQualifiedClassName("com.example.MyClass");

            assertEquals("com.example.MyClass", info.getFullQualifiedClassName());
        }

        @Test
        void setSuperClassesFullNameUpdatesValue() {
            ClassInfo info = new ClassInfo();
            info.setSuperClassesFullName(Arrays.asList("A", "B", "C"));

            assertEquals(3, info.getSuperClassesFullName().size());
        }

        @Test
        void setFieldsUpdatesValue() {
            ClassInfo info = new ClassInfo();
            info.setFields(Collections.singletonList(new FieldInfo("id", "Long")));

            assertEquals(1, info.getFields().size());
            assertEquals("id", info.getFields().get(0).getFieldName());
        }

        @Test
        void setConstructorUpdatesValue() {
            ClassInfo info = new ClassInfo();
            ConstructorInfo ctor = new ConstructorInfo(Collections.emptyList());
            info.setConstructor(ctor);

            assertNotNull(info.getConstructor());
            assertEquals(0, info.getConstructor().getParametersDataTypes().size());
        }

        @Test
        void setPostConstructUpdatesValue() {
            ClassInfo info = new ClassInfo();
            info.setPostConstruct(new MethodInfo("startup", "void"));

            assertEquals("startup", info.getPostConstruct().getMethodName());
        }

        @Test
        void setCreateInstanceMethodsUpdatesValue() {
            ClassInfo info = new ClassInfo();
            info.setCreateInstanceMethods(Arrays.asList(
                    new MethodInfo("build", "Service"),
                    new MethodInfo("create", "Service")
            ));

            assertEquals(2, info.getCreateInstanceMethods().size());
        }

        @Test
        void settersOverrideConstructorValues() {
            ClassInfo info = new ClassInfo(
                    "Original", Collections.emptyList(), Collections.emptyList(),
                    null, null, Collections.emptyList()
            );
            info.setFullQualifiedClassName("Updated");

            assertEquals("Updated", info.getFullQualifiedClassName());
        }
    }
}
