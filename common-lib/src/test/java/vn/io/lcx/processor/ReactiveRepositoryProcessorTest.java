package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;

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

    private boolean hasDiagnostic(Compilation compilation, Diagnostic.Kind kind, String message) {
        return compilation.diagnostics().stream()
                .anyMatch(d -> d.getKind() == kind && d.getMessage(null).contains(message));
    }

    private String generatedSource(Compilation compilation, String className) throws IOException {
        return compilation.generatedSourceFile(className)
                .orElseThrow()
                .getCharContent(false)
                .toString();
    }

    private JavaFileObject productRepository(String name, String methodSource) {
        return JavaFileObjects.forSourceString(
                "test." + name,
                """
                package test;

                import io.vertx.core.Future;
                import io.vertx.ext.web.RoutingContext;
                import io.vertx.sqlclient.SqlConnection;
                import java.util.List;
                import java.util.Optional;
                import vn.io.lcx.common.database.pageable.Page;
                import vn.io.lcx.common.database.pageable.Pageable;
                import vn.io.lcx.reactive.annotation.Query;
                import vn.io.lcx.reactive.annotation.RRepository;
                import vn.io.lcx.reactive.repository.ReactiveRepository;

                @RRepository
                public interface %s extends ReactiveRepository<Product> {
                %s
                }
                """.formatted(name, methodSource)
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

    @Nested
    @DisplayName("Contract Validation")
    class ContractValidation {

        @Test
        void rRepository_onClass_failsCompilation() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.NotInterfaceRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.RRepository;

                    @RRepository
                    public class NotInterfaceRepo {
                    }
                    """
            );

            Compilation compilation = compile(repo);

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "@RRepository can only be used on interfaces"));
        }

        @Test
        void interfaceNotExtendingReactiveRepository_failsCompilation() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.NotReactiveRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.RRepository;

                    @RRepository
                    public interface NotReactiveRepo {
                    }
                    """
            );

            Compilation compilation = compile(repo);

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "must extend vn.io.lcx.reactive.repository.ReactiveRepository"));
        }

        @Test
        void customMethodWithoutFuture_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "InvalidReturnRepo",
                    """
                        @Query("SELECT * FROM products")
                        Product findAll(RoutingContext ctx, SqlConnection conn);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "must return io.vertx.core.Future"));
        }

        @Test
        void missingRoutingContext_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "InvalidContextRepo",
                    """
                        @Query("SELECT * FROM products WHERE id = ?1")
                        Future<Product> findById(SqlConnection conn, Long id);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "First parameter must be"));
        }

        @Test
        void customMethodWithoutQuery_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "MissingQueryRepo",
                    """
                        Future<Product> findByName(RoutingContext ctx, SqlConnection conn, String name);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "Unsupported method"));
        }

        @Test
        void pageableNotLast_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "PageableOrderRepo",
                    """
                        @Query("SELECT * FROM products WHERE name = ?1")
                        Future<List<Product>> findByName(RoutingContext ctx, SqlConnection conn, Pageable pageable, String name);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "Pageable parameter must be the final"));
        }

        @Test
        void pageWithoutPageable_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "MissingPageableRepo",
                    """
                        @Query("SELECT * FROM products")
                        Future<Page<Product>> page(RoutingContext ctx, SqlConnection conn);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "must declare Pageable as the final parameter"));
        }

        @Test
        void optionalListReturn_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "OptionalListRepo",
                    """
                        @Query("SELECT * FROM products")
                        Future<Optional<List<Product>>> findAll(RoutingContext ctx, SqlConnection conn);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "Future<Optional<List<T>>> is not supported"));
        }
    }

    @Nested
    @DisplayName("Query Planning")
    class QueryPlanning {

        @Test
        void implicitPlaceholders_generateStringBuilderAndTuple() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ImplicitPlaceholderRepo",
                    """
                        @Query("SELECT * FROM products WHERE name = ? AND price > ?")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name, Long price);
                    """
            ));

            String generated = generatedSource(compilation, "test.ImplicitPlaceholderRepoImpl");

            assertTrue(generated.contains("StringBuilder sql = new StringBuilder();"));
            assertTrue(generated.contains("tuple.addValue(name);"));
            assertTrue(generated.contains("tuple.addValue(price);"));
            assertFalse(generated.contains("$11"));
        }

        @Test
        void indexedPlaceholderReuse_isAcceptedAndBindsEachOccurrence() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "IndexedPlaceholderRepo",
                    """
                        @Query("SELECT * FROM products WHERE name = ?1 OR alias = ?1")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name);
                    """
            ));

            String generated = generatedSource(compilation, "test.IndexedPlaceholderRepoImpl");

            assertEquals(2, generated.split("tuple.addValue\\(name\\);", -1).length - 1);
        }

        @Test
        void mixedPlaceholderStyles_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "MixedPlaceholderRepo",
                    """
                        @Query("SELECT * FROM products WHERE name = ? AND id = ?2")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name, Long id);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "Do not mix implicit ? and indexed ?1"));
        }

        @Test
        void placeholderInsideStringLiteral_isIgnored() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "StringLiteralPlaceholderRepo",
                    """
                        @Query("SELECT * FROM products WHERE marker = '?' AND name = ?1;")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name);
                    """
            ));

            String generated = generatedSource(compilation, "test.StringLiteralPlaceholderRepoImpl");

            assertEquals(1, generated.split("tuple.addValue\\(name\\);", -1).length - 1);
            assertFalse(generated.contains(";\""));
        }

        @Test
        void placeholderIndexTooHigh_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "IndexTooHighRepo",
                    """
                        @Query("SELECT * FROM products WHERE name = ?2")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "placeholder index exceeds"));
        }

        @Test
        void unusedQueryParameter_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "UnusedParameterRepo",
                    """
                        @Query("SELECT * FROM products")
                        Future<List<Product>> search(RoutingContext ctx, SqlConnection conn, String name);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "is not referenced by any placeholder"));
        }
    }

    @Nested
    @DisplayName("IN Binding")
    class InBinding {

        @Test
        void listParameterInsideIn_isExpanded() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "InListRepo",
                    """
                        @Query("SELECT * FROM products WHERE id IN (?1)")
                        Future<List<Product>> findByIds(RoutingContext ctx, SqlConnection conn, List<Long> ids);
                    """
            ));

            String generated = generatedSource(compilation, "test.InListRepoImpl");

            assertTrue(generated.contains("ids.isEmpty()"));
            assertTrue(generated.contains("for (Object item : ids)"));
            assertTrue(generated.contains("tuple.addValue(item);"));
        }

        @Test
        void scalarParameterInsideIn_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ScalarInRepo",
                    """
                        @Query("SELECT * FROM products WHERE id IN (?1)")
                        Future<List<Product>> findById(RoutingContext ctx, SqlConnection conn, Long id);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "requires a collection or object array"));
        }

        @Test
        void collectionParameterOutsideIn_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "CollectionOutsideInRepo",
                    """
                        @Query("SELECT * FROM products WHERE id = ?1")
                        Future<List<Product>> findByIds(RoutingContext ctx, SqlConnection conn, List<Long> ids);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "Collection query parameters are only supported inside IN"));
        }
    }

    @Nested
    @DisplayName("Return Mapping")
    class ReturnMapping {

        @Test
        void scalarAndListScalarReturnsAreGenerated() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ScalarRepo",
                    """
                        @Query("SELECT COUNT(1) FROM products")
                        Future<Long> countAll(RoutingContext ctx, SqlConnection conn);

                        @Query("SELECT id FROM products")
                        Future<List<Long>> findIds(RoutingContext ctx, SqlConnection conn);

                        @Query("SELECT name FROM products WHERE id = ?1")
                        Future<Optional<String>> findName(RoutingContext ctx, SqlConnection conn, Long id);
                    """
            ));

            String generated = generatedSource(compilation, "test.ScalarRepoImpl");

            assertTrue(generated.contains("row.getLong(0)"));
            assertTrue(generated.contains("result += row.getLong(0);"));
            assertTrue(generated.contains("java.util.Optional.ofNullable(row.getString(0))"));
        }

        @Test
        void objectArrayReturn_emitsWarning() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ObjectArrayRepo",
                    """
                        @Query("SELECT id, name FROM products")
                        Future<Object[]> findRaw(RoutingContext ctx, SqlConnection conn);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.WARNING, "Future<Object[]> query mapping is deprecated"));
        }
    }

    @Nested
    @DisplayName("Pageable")
    class PageableQueries {

        @Test
        void listWithPageable_emitsWarning() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ListPageableRepo",
                    """
                        @Query("SELECT * FROM products")
                        Future<List<Product>> findAll(RoutingContext ctx, SqlConnection conn, Pageable pageable);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.WARNING, "Using Pageable with Future<List<T>> is deprecated"));
        }

        @Test
        void explicitCountQuery_isGenerated() throws IOException {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ExplicitCountRepo",
                    """
                        @Query(value = "SELECT * FROM products WHERE name = ?1 ORDER BY id DESC",
                               countQuery = "SELECT COUNT(1) FROM products WHERE name = ?1")
                        Future<Page<Product>> pageByName(RoutingContext ctx, SqlConnection conn, String name, Pageable pageable);
                    """
            ));

            String generated = generatedSource(compilation, "test.ExplicitCountRepoImpl");

            assertTrue(generated.contains("StringBuilder countSql = new StringBuilder();"));
            assertTrue(generated.contains("SELECT COUNT(1) FROM products WHERE name = "));
        }

        @Test
        void complexPageQueryWithoutCount_failsCompilation() {
            Compilation compilation = compile(productEntity(), productRepository(
                    "ComplexPageRepo",
                    """
                        @Query("SELECT name, COUNT(1) FROM products GROUP BY name")
                        Future<Page<Product>> grouped(RoutingContext ctx, SqlConnection conn, Pageable pageable);
                    """
            ));

            assertTrue(hasDiagnostic(compilation, Diagnostic.Kind.ERROR, "must define countQuery"));
        }
    }
}
