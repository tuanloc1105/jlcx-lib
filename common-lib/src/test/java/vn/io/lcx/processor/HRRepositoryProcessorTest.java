package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

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

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        void hrRepository_generatesImpl() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.BookRepository",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface BookRepository extends HReactiveRepository<Book> {
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("BookRepositoryImpl")),
                    "Should generate {Name}Impl class");
        }

        @Test
        void generatedImpl_implementsInterface() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.ArticleRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface ArticleRepo extends HReactiveRepository<Book> {
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ArticleRepoImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("ArticleRepo"),
                    "Generated impl should reference the interface");
        }

        @Test
        void generatedImpl_samePackageAsInterface() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.data.DataRepo",
                    """
                    package test.data;

                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;
                    import test.Book;

                    @HRRepository
                    public interface DataRepo extends HReactiveRepository<Book> {
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("DataRepoImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("package test.data"),
                    "Generated impl should be in same package");
        }
    }

    @Nested
    @DisplayName("Entity Type Resolution")
    class EntityTypeResolution {

        @Test
        void genericType_resolvedToEntityClass() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.TypedRepo",
                    """
                    package test;

                    import vn.io.lcx.reactive.annotation.HRRepository;
                    import vn.io.lcx.reactive.repository.HReactiveRepository;

                    @HRRepository
                    public interface TypedRepo extends HReactiveRepository<Book> {
                    }
                    """
            );

            Compilation compilation = compile(bookEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("TypedRepoImpl"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("Book"),
                    "Generated code should reference entity type Book");
        }
    }
}
