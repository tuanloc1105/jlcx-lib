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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HRRepositoryProcessor")
class HRRepositoryProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new HRRepositoryProcessor())
                .compile(sources);
    }

    private JavaFileObject bookEntity() {
        return JavaFileObjects.forSourceString(
                "test.Book",
                """
                package test;

                public class Book {
                    private Long id;
                    private String title;
                    private String author;

                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                    public String getAuthor() { return author; }
                    public void setAuthor(String author) { this.author = author; }
                }
                """
        );
    }

    private JavaFileObject bookSummary() {
        return JavaFileObjects.forSourceString(
                "test.BookSummary",
                """
                package test;

                public class BookSummary {
                    private Long id;
                    private String title;

                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                }
                """
        );
    }

    private JavaFileObject repository(String name, String methods) {
        return JavaFileObjects.forSourceString(
                "test." + name,
                """
                package test;

                import io.vertx.core.Future;
                import org.hibernate.reactive.stage.Stage;
                import java.util.List;
                import java.util.Optional;
                import vn.io.lcx.common.database.pageable.Page;
                import vn.io.lcx.common.database.pageable.Pageable;
                import vn.io.lcx.reactive.annotation.HRModifying;
                import vn.io.lcx.reactive.annotation.HRParam;
                import vn.io.lcx.reactive.annotation.HRQuery;
                import vn.io.lcx.reactive.annotation.HRRepository;
                import vn.io.lcx.reactive.annotation.HRResultSetMapping;
                import vn.io.lcx.reactive.repository.HReactiveRepository;

                @HRRepository
                public interface %s extends HReactiveRepository<Book> {
                %s
                }
                """.formatted(name, methods)
        );
    }

    private boolean hasError(Compilation compilation, String message) {
        return compilation.diagnostics().stream()
                .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
                        && d.getMessage(null).contains(message));
    }

    private String generatedSource(Compilation compilation, String className) throws IOException {
        String sourcePath = className.replace('.', '/') + ".java";
        return compilation.generatedSourceFiles().stream()
                .filter(file -> file.getName().endsWith(sourcePath))
                .findFirst()
                .orElseThrow()
                .getCharContent(false)
                .toString();
    }

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        void hrRepository_generatesImpl() {
            Compilation compilation = compile(bookEntity(), repository("BookRepository", ""));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("BookRepositoryImpl")));
        }

        @Test
        void generatedImpl_samePackageAsInterface() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.data.DataRepo",
                    """
                    package test.data;

                    import test.Book;
                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface DataRepo extends HReactiveRepository<Book> {
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.data.DataRepoImpl");
            assertTrue(code.contains("package test.data"));
        }

        @Test
        void generatedHrCode_doesNotCastToPageableImpl() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "PageRepo",
                    """
                        @HRQuery(
                            value = "from Book b where b.author = ?1 order by b.title",
                            countQuery = "select count(b) from Book b where b.author = ?1"
                        )
                        Future<Page<Book>> findPage(Stage.Session session, String author, Pageable pageable);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.PageRepoImpl");
            assertFalse(code.contains("PageableImpl"));
            assertTrue(code.contains("pageable.getOffset()"));
            assertTrue(code.contains("pageable.getPageNumber()"));
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        void hrRepositoryOnClass_fails() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.NotInterface",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.HRRepository;

                    @HRRepository
                    public class NotInterface {
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "@HRRepository can only be used on interfaces"));
        }

        @Test
        void missingHReactiveRepository_fails() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.MissingBaseRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.HRRepository;

                    @HRRepository
                    public interface MissingBaseRepo {
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "must extend vn.io.lcx.reactive.repository.HReactiveRepository<T>"));
        }

        @Test
        void wrongFirstParameter_fails() {
            Compilation compilation = compile(bookEntity(), repository(
                    "WrongSessionRepo",
                    """
                        @HRQuery("from Book b")
                        Future<List<Book>> findAll(String session);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "First parameter must be org.hibernate.reactive.stage.Stage.Session"));
        }

        @Test
        void wrongJpaQueryImport_failsWithHrQueryMessage() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.WrongJpaQueryRepo",
                    """
                    package test;

                    import io.vertx.core.Future;
                    import java.util.List;
                    import org.hibernate.reactive.stage.Stage;
                    import vn.io.lcx.jpa.annotation.Query;
                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface WrongJpaQueryRepo extends HReactiveRepository<Book> {
                        @Query("from Book b")
                        Future<List<Book>> findAll(Stage.Session session);
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "Use @vn.io.lcx.reactive.annotation.HRQuery"));
        }

        @Test
        void wrongReactiveQueryImport_failsWithHrQueryMessage() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.WrongReactiveQueryRepo",
                    """
                    package test;

                    import io.vertx.core.Future;
                    import java.util.List;
                    import org.hibernate.reactive.stage.Stage;
                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.annotation.Query;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface WrongReactiveQueryRepo extends HReactiveRepository<Book> {
                        @Query("from Book b")
                        Future<List<Book>> findAll(Stage.Session session);
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "instead of @vn.io.lcx.reactive.annotation.Query"));
        }

        @Test
        void wrongJpaParamAndModifyingImports_failWithHrMessages() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.WrongJpaAnnotationsRepo",
                    """
                    package test;

                    import io.vertx.core.Future;
                    import org.hibernate.reactive.stage.Stage;
                    import vn.io.lcx.jpa.annotation.Modifying;
                    import vn.io.lcx.jpa.annotation.Param;
                    import vn.io.lcx.reactive.annotation.HRQuery;
                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface WrongJpaAnnotationsRepo extends HReactiveRepository<Book> {
                        @Modifying
                        @HRQuery("update Book b set b.title = :title")
                        Future<Integer> updateTitle(Stage.Session session, @Param("title") String title);
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), source);
            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "Use @vn.io.lcx.reactive.annotation.HRModifying"));
            assertTrue(hasError(compilation, "Use @vn.io.lcx.reactive.annotation.HRParam"));
        }

        @Test
        void pageQueryWithoutPageable_fails() {
            Compilation compilation = compile(bookEntity(), repository(
                    "PageWithoutPageableRepo",
                    """
                        @HRQuery("from Book b")
                        Future<Page<Book>> findPage(Stage.Session session);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "Future<Page<T>> HR query methods must declare Pageable"));
        }

        @Test
        void complexPageQueryWithoutCountQuery_fails() {
            Compilation compilation = compile(bookEntity(), repository(
                    "ComplexPageRepo",
                    """
                        @HRQuery("select distinct b from Book b where b.author = ?1")
                        Future<Page<Book>> findPage(Stage.Session session, String author, Pageable pageable);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "must define countQuery()"));
        }

        @Test
        void placeholderMismatch_fails() {
            Compilation compilation = compile(bookEntity(), repository(
                    "PlaceholderMismatchRepo",
                    """
                        @HRQuery("from Book b where b.title = ?1 and b.author = ?2")
                        Future<List<Book>> findByTitle(Stage.Session session, String title);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "placeholder index exceeds repository method parameter count"));
        }

        @Test
        void unusedParameter_fails() {
            Compilation compilation = compile(bookEntity(), repository(
                    "UnusedParameterRepo",
                    """
                        @HRQuery("from Book b where b.title = ?1")
                        Future<List<Book>> findByTitle(Stage.Session session, String title, String author);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "is not referenced by any placeholder"));
        }

        @Test
        void mixedPlaceholders_fail() {
            Compilation compilation = compile(bookEntity(), repository(
                    "MixedPlaceholderRepo",
                    """
                        @HRQuery("from Book b where b.title = ?1 and b.author = ?")
                        Future<List<Book>> findByTitle(Stage.Session session, String title, String author);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "Do not mix implicit ?, indexed ?1, and named :name"));
        }

        @Test
        void namedPlaceholdersRequireHrParam() {
            Compilation compilation = compile(bookEntity(), repository(
                    "NamedParamRepo",
                    """
                        @HRQuery("from Book b where b.title = :title")
                        Future<List<Book>> findByTitle(Stage.Session session, String title);
                    """
            ));

            assertEquals(Compilation.Status.FAILURE, compilation.status());
            assertTrue(hasError(compilation, "Named placeholders require @HRParam"));
        }
    }

    @Nested
    @DisplayName("Query Generation")
    class QueryGeneration {

        @Test
        void hqlOptional_usesSingleResultOrNull() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "OptionalRepo",
                    """
                        @HRQuery("from Book b where b.title = ?1")
                        Future<Optional<Book>> findByTitle(Stage.Session session, String title);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.OptionalRepoImpl");
            assertTrue(code.contains("getSingleResultOrNull()"));
            assertTrue(code.contains("java.util.Optional::ofNullable"));
        }

        @Test
        void hqlList_usesResultList() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "ListRepo",
                    """
                        @HRQuery("from Book b where b.author = ?1")
                        Future<List<Book>> findByAuthor(Stage.Session session, String author);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.ListRepoImpl");
            assertTrue(code.contains("getResultList()"));
        }

        @Test
        void nativeDtoResultMapping_usesMethodReturnType() throws Exception {
            Compilation compilation = compile(bookEntity(), bookSummary(), repository(
                    "NativeMappingRepo",
                    """
                        @HRResultSetMapping(name = "BookSummaryMapping")
                        @HRQuery(value = "select id, title from books where id = ?1", isNative = true)
                        Future<Optional<BookSummary>> findSummary(Stage.Session session, Long id);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.NativeMappingRepoImpl");
            assertTrue(code.contains("ResultSetMapping<test.BookSummary>"));
            assertTrue(code.contains("return test.BookSummary.class;"));
            assertTrue(code.contains("BookSummaryMapping"));
        }

        @Test
        void hrModifyingInteger_generatesExecuteUpdate() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "UpdateCountRepo",
                    """
                        @HRModifying
                        @HRQuery("update Book b set b.title = ?1 where b.id = ?2")
                        Future<Integer> updateTitle(Stage.Session session, String title, Long id);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.UpdateCountRepoImpl");
            assertTrue(code.contains("createMutationQuery"));
            assertTrue(code.contains("query.executeUpdate()"));
        }

        @Test
        void hrModifyingVoid_mapsResultToNull() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "UpdateVoidRepo",
                    """
                        @HRModifying
                        @HRQuery("delete from Book b where b.id = ?1")
                        Future<Void> deleteById(Stage.Session session, Long id);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.UpdateVoidRepoImpl");
            assertTrue(code.contains("query.executeUpdate()).map(v -> null)"));
        }

        @Test
        void pageQueryWithExplicitCount_generatesListAndCountQueries() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "ExplicitCountRepo",
                    """
                        @HRQuery(
                            value = "from Book b where b.author = ?1 order by b.title",
                            countQuery = "select count(b) from Book b where b.author = ?1"
                        )
                        Future<Page<Book>> findPage(Stage.Session session, String author, Pageable pageable);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.ExplicitCountRepoImpl");
            assertTrue(code.contains("from Book b where b.author = ?1 order by b.title"));
            assertTrue(code.contains("select count(b) from Book b where b.author = ?1"));
            assertTrue(code.contains("countQuery.getSingleResult()"));
            assertTrue(code.contains("Page.<test.Book>create"));
        }

        @Test
        void namedHrParam_generatesNamedBinding() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "NamedBindingRepo",
                    """
                        @HRQuery("from Book b where b.title = :title")
                        Future<List<Book>> findByTitle(Stage.Session session, @HRParam("title") String title);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.NamedBindingRepoImpl");
            assertTrue(code.contains("query.setParameter(\"title\", title);"));
        }

        @Test
        void queryStringEscaping_generatesValidJavaLiteral() throws Exception {
            Compilation compilation = compile(bookEntity(), repository(
                    "EscapedQueryRepo",
                    """
                        @HRQuery(\"\"\"
                                from Book b
                                where b.title = "special"
                                  and b.author = ?1
                                  and b.title <> 'C:\\\\tmp'
                                \"\"\")
                        Future<List<Book>> escaped(Stage.Session session, String author);
                    """
            ));
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            String code = generatedSource(compilation, "test.EscapedQueryRepoImpl");
            assertTrue(code.contains("\\n"));
            assertTrue(code.contains("\\\"special\\\""));
            assertTrue(code.contains("\\\\tmp"));
        }
    }
}
