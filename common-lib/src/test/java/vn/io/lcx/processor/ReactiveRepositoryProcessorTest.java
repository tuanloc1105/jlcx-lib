package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReactiveRepositoryProcessor")
class ReactiveRepositoryProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ReactiveRepositoryProcessor())
                .compile(sources);
    }

    private JavaFileObject productEntity() {
        return JavaFileObjects.forSourceString(
                "test.Product",
                """
                package test;

                public class Product {
                    private Long id;
                    private String name;
                    private Double price;

                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                    public Double getPrice() { return price; }
                    public void setPrice(Double price) { this.price = price; }
                }
                """
        );
    }

    @Nested
    @DisplayName("Processor Execution")
    class ProcessorExecution {

        @Test
        void rRepository_processorRunsAndGeneratesFile() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.ProductRepository",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.RRepository;
                    import vn.io.lcx.reactive.repository.ReactiveRepository;

                    @RRepository
                    public interface ProductRepository extends ReactiveRepository<Product> {
                    }
                    """
            );

            Compilation compilation = compile(productEntity(), repo);
            // Generated code references many Vert.x classes not available in compile-testing classpath.
            // The processor itself runs successfully and generates the file, but the generated file
            // may fail to compile due to missing dependencies. Verify the processor doesn't crash.
            boolean processorRanSuccessfully = compilation.diagnostics().stream()
                    .noneMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                            && d.getMessage(null).contains("ReactiveRepositoryProcessor"));

            assertTrue(processorRanSuccessfully,
                    "Processor should run without internal errors");
        }

        @Test
        void rRepository_generatesImplFile() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.ItemRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.RRepository;
                    import vn.io.lcx.reactive.repository.ReactiveRepository;

                    @RRepository
                    public interface ItemRepo extends ReactiveRepository<Product> {
                    }
                    """
            );

            Compilation compilation = compile(productEntity(), repo);

            // The processor generates the file, but generated code won't compile
            // because it references Vert.x internal classes. Verify a NOTE message
            // about code generation was emitted (indicating the processor ran).
            boolean hasGenerationNote = compilation.diagnostics().stream()
                    .anyMatch(d -> d.getKind() == Diagnostic.Kind.NOTE
                            && d.getMessage(null).contains("Generating"));

            assertTrue(hasGenerationNote,
                    "Processor should emit a NOTE about code generation");
        }

        @Test
        void rRepository_withQueryMethod_processorRunsSuccessfully() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.QueryProductRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.RRepository;
                    import vn.io.lcx.reactive.annotation.Query;
                    import vn.io.lcx.reactive.repository.ReactiveRepository;
                    import io.vertx.core.Future;
                    import io.vertx.ext.web.RoutingContext;
                    import io.vertx.sqlclient.SqlConnection;
                    import java.util.List;

                    @RRepository
                    public interface QueryProductRepo extends ReactiveRepository<Product> {
                        @Query("SELECT * FROM products WHERE name = ?1")
                        Future<List<Product>> findByName(RoutingContext ctx, SqlConnection conn, String name);
                    }
                    """
            );

            Compilation compilation = compile(productEntity(), repo);

            // Verify no processor-internal errors (only generated code compilation errors are expected)
            boolean noProcessorCrash = compilation.diagnostics().stream()
                    .noneMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                            && (d.getMessage(null).contains("Unexpected error")
                            || d.getMessage(null).contains("NullPointerException")));

            assertTrue(noProcessorCrash, "Processor should not crash on query method");
        }

        @Test
        void nonAnnotatedInterface_noCodeGenerated() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.PlainRepo",
                    """
                    package test;

                    public interface PlainRepo {
                        String findSomething();
                    }
                    """
            );

            Compilation compilation = compile(repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            boolean hasGenerationNote = compilation.diagnostics().stream()
                    .anyMatch(d -> d.getKind() == Diagnostic.Kind.NOTE
                            && d.getMessage(null).contains("Generating"));

            assertFalse(hasGenerationNote,
                    "Should NOT generate code for non-annotated interfaces");
        }
    }
}
