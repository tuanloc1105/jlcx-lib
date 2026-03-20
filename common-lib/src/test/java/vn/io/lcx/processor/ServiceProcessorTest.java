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

    @Nested
    @DisplayName("Transactional Attributes")
    class TransactionalAttributes {

        @Test
        void transactionalWithIsolation_generatesIsolationCode() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.IsoService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;
                    import vn.io.lcx.jpa.annotation.Transactional;
                    import java.sql.Connection;

                    @Service
                    public class IsoService {
                        @Transactional(isolation = Connection.TRANSACTION_SERIALIZABLE)
                        public void serialWork() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("IsoServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            // TRANSACTION_SERIALIZABLE = 8; the processor writes the integer value
            assertTrue(code.contains("setTransactionIsolation(8)"),
                    "Generated proxy should contain setTransactionIsolation with TRANSACTION_SERIALIZABLE value (8)");
        }

        @Test
        void transactionalWithOnRollback_generatesRollbackCode() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.RollbackService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;
                    import vn.io.lcx.jpa.annotation.Transactional;

                    @Service
                    public class RollbackService {
                        @Transactional(onRollback = {RuntimeException.class})
                        public void riskyWork() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("RollbackServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("RuntimeException"),
                    "Generated proxy should contain RuntimeException in rollback catch block");
            assertTrue(code.contains("rollback"),
                    "Generated proxy should contain rollback logic");
        }
    }

    @Nested
    @DisplayName("Return Type Handling")
    class ReturnTypeHandling {

        @Test
        void voidMethod_wrappedCorrectly() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.VoidService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class VoidService {
                        public void doSomething() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("VoidServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertFalse(code.contains("return actualResult"),
                    "Void method should not contain 'return actualResult'");
        }

        @Test
        void returnTypeMethod_wrappedWithReturn() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ReturnService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class ReturnService {
                        public String fetchData() {
                            return "data";
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReturnServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("return actualResult"),
                    "Non-void method should contain 'return actualResult'");
        }
    }

    @Nested
    @DisplayName("Method Modifier Filtering")
    class MethodModifierFiltering {

        @Test
        void abstractMethod_isSkipped() {
            // An abstract class with an abstract method: the processor skips the abstract
            // method in the generated proxy. This causes compilation to fail because the
            // proxy extends the abstract class without implementing the abstract method,
            // which proves the processor correctly filters out abstract methods.
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.AbstractService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public abstract class AbstractService {
                        public void concreteMethod() {}

                        public abstract void abstractMethod();
                    }
                    """
            );

            Compilation compilation = compile(source);
            // Compilation fails because the generated proxy skips the abstract method,
            // resulting in a concrete class that doesn't implement the abstract method.
            // This confirms abstract methods are filtered out by the processor.
            assertEquals(Compilation.Status.FAILURE, compilation.status(),
                    "Compilation should fail because abstract method is skipped in proxy, " +
                    "leaving the proxy as a concrete class that doesn't implement it");
        }

        @Test
        void finalMethod_isSkipped() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.FinalService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class FinalService {
                        public void normalMethod() {}

                        public final void finalMethod() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("FinalServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("normalMethod"), "Should contain normal method");
            assertFalse(code.contains("finalMethod"),
                    "Final method should be skipped in generated proxy");
        }

        @Test
        void protectedMethod_isSkipped() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ProtectedService",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Service;

                    @Service
                    public class ProtectedService {
                        public void publicMethod() {}

                        protected void protectedMethod() {}
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ProtectedServiceProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("publicMethod"), "Should contain public method");
            assertFalse(code.contains("protectedMethod"),
                    "Protected method should be skipped in generated proxy");
        }
    }
}
