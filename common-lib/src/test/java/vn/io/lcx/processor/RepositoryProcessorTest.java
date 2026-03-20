package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepositoryProcessor")
class RepositoryProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new RepositoryProcessor())
                .compile(sources);
    }

    private JavaFileObject testEntity() {
        return JavaFileObjects.forSourceString(
                "test.TestEntity",
                """
                package test;

                import jakarta.persistence.Id;

                public class TestEntity {
                    @Id
                    private Long id;
                    private String name;

                    public Long getId() { return id; }
                    public void setId(Long id) { this.id = id; }
                    public String getName() { return name; }
                    public void setName(String name) { this.name = name; }
                }
                """
        );
    }

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        void repositoryInterface_generatesProxy() {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.TestEntityRepository",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Repository;
                    import vn.io.lcx.jpa.respository.JpaRepository;

                    @Repository
                    public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
                    }
                    """
            );

            Compilation compilation = compile(testEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("TestEntityRepositoryProxy")),
                    "Should generate {Name}Proxy class");
        }

        @Test
        void generatedProxy_implementsInterface() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.UserRepo",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Repository;
                    import vn.io.lcx.jpa.respository.JpaRepository;

                    @Repository
                    public interface UserRepo extends JpaRepository<TestEntity, Long> {
                    }
                    """
            );

            Compilation compilation = compile(testEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("UserRepoProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("UserRepo"),
                    "Generated proxy should reference the interface");
        }

        @Test
        void generatedProxy_samePackageAsInterface() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.repo.PkgRepo",
                    """
                    package test.repo;

                    import vn.io.lcx.jpa.annotation.Repository;
                    import vn.io.lcx.jpa.respository.JpaRepository;
                    import test.TestEntity;

                    @Repository
                    public interface PkgRepo extends JpaRepository<TestEntity, Long> {
                    }
                    """
            );

            Compilation compilation = compile(testEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("PkgRepoProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("package test.repo"),
                    "Generated proxy should be in same package");
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        void queryAnnotatedMethod_generatesImplementation() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.QueryRepo",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Repository;
                    import vn.io.lcx.jpa.annotation.Query;
                    import vn.io.lcx.jpa.respository.JpaRepository;
                    import java.util.List;

                    @Repository
                    public interface QueryRepo extends JpaRepository<TestEntity, Long> {
                        @Query("SELECT e FROM TestEntity e WHERE e.name = ?1")
                        List<TestEntity> findByName(String name);
                    }
                    """
            );

            Compilation compilation = compile(testEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("QueryRepoProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("findByName"), "Should contain custom query method");
            assertTrue(code.contains("SELECT e FROM TestEntity"),
                    "Should contain the query string");
        }
    }

    @Nested
    @DisplayName("Entity Type Resolution")
    class EntityTypeResolution {

        @Test
        void repositoryWithGenericTypes_resolvedCorrectly() throws Exception {
            JavaFileObject repo = JavaFileObjects.forSourceString(
                    "test.GenericRepo",
                    """
                    package test;

                    import vn.io.lcx.jpa.annotation.Repository;
                    import vn.io.lcx.jpa.respository.JpaRepository;

                    @Repository
                    public interface GenericRepo extends JpaRepository<TestEntity, Long> {
                    }
                    """
            );

            Compilation compilation = compile(testEntity(), repo);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("GenericRepoProxy"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("TestEntity"),
                    "Generated code should reference entity type");
        }
    }
}
