package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceProcessor")
class ServiceProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ServiceProcessor())
                .compile(sources);
    }

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        void serviceClass_generatesProxy() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.UserService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class UserService {
                        public String findUser() {
                            return "user";
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("UserServiceProxy")),
                    "Should generate {Name}Proxy class");
        }

        @Test
        void generatedProxy_extendsOriginalClass() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.OrderService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class OrderService {
                        public void processOrder() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("OrderServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("extends OrderService"),
                    "Generated proxy should extend original service class");
        }

        @Test
        void generatedProxy_wrapsPublicMethods() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.CalcService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class CalcService {
                        public int add(int a, int b) {
                            return a + b;
                        }

                        public int multiply(int a, int b) {
                            return a * b;
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("CalcServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("add"), "Should wrap add method");
            assertTrue(code.contains("multiply"), "Should wrap multiply method");
        }
    }

    @Nested
    @DisplayName("Transaction Support")
    class TransactionSupport {

        @Test
        void transactionalMethod_generatesTransactionWrapper() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.TxService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;
                    import vn.io.lcx.jpa.annotation.Transactional;

                    @Service
                    public class TxService {
                        @Transactional
                        public void doWork() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("TxServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("JpaContext") || code.contains("jpaContext") || code.contains("transaction")
                            || code.contains("commit") || code.contains("rollback"),
                    "Generated proxy should contain transaction management code");
        }
    }

    @Nested
    @DisplayName("Method Filtering")
    class MethodFiltering {

        @Test
        void privateAndStaticMethods_areSkipped() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.FilterService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class FilterService {
                        public void publicMethod() {}

                        private void privateMethod() {}

                        static void staticMethod() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("FilterServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("publicMethod"), "Should wrap public method");
            assertFalse(code.contains("privateMethod"), "Should skip private method");
            assertFalse(code.contains("staticMethod"), "Should skip static method");
        }
    }

    @Nested
    @DisplayName("Package Handling")
    class PackageHandling {

        @Test
        void generatedProxy_samePackageAsOriginal() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.pkg.MyService",
                    """
                    package test.pkg;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class MyService {
                        public void execute() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("MyServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("package test.pkg"),
                    "Generated proxy should be in same package");
        }
    }
}
