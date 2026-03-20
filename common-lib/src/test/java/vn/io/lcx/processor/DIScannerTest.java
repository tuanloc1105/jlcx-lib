package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DIScanner")
class DIScannerTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new DIScanner())
                .compile(sources);
    }

    @Test
    void componentClass_generatesClassIndexJson() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.MyComponent",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class MyComponent {
                    public MyComponent() {}
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        boolean hasClassIndex = compilation.generatedFiles().stream()
                .anyMatch(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"));
        assertTrue(hasClassIndex, "Should generate META-INF/class-index-*.json");
    }

    @Test
    void componentClass_jsonContainsClassName() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.ScannerTestComponent",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class ScannerTestComponent {
                    private String name;

                    public ScannerTestComponent() {}
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        JavaFileObject jsonFile = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String json = jsonFile.getCharContent(false).toString();
        assertTrue(json.contains("test.ScannerTestComponent"),
                "JSON should contain full qualified class name");
    }

    @Test
    void componentWithConstructorParams_jsonContainsConstructorInfo() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.ServiceComponent",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class ServiceComponent {
                    private final String config;

                    public ServiceComponent(String config) {
                        this.config = config;
                    }
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        JavaFileObject jsonFile = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String json = jsonFile.getCharContent(false).toString();
        assertTrue(json.contains("test.ServiceComponent"));
        assertTrue(json.contains("java.lang.String"), "JSON should contain constructor parameter types");
    }

    @Test
    void componentWithPostConstruct_jsonContainsPostConstructInfo() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.PostConstructComponent",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;
                import vn.io.lcx.common.annotation.PostConstruct;

                @Component
                public class PostConstructComponent {
                    public PostConstructComponent() {}

                    @PostConstruct
                    public void init() {}
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        JavaFileObject jsonFile = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String json = jsonFile.getCharContent(false).toString();
        assertTrue(json.contains("init"), "JSON should contain @PostConstruct method name");
    }

    @Test
    void componentWithInstanceMethod_jsonContainsCreateInstanceMethods() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.FactoryComponent",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;
                import vn.io.lcx.common.annotation.Instance;

                @Component
                public class FactoryComponent {
                    public FactoryComponent() {}

                    @Instance
                    public String createName() {
                        return "test";
                    }
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        JavaFileObject jsonFile = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String json = jsonFile.getCharContent(false).toString();
        assertTrue(json.contains("createName"), "JSON should contain @Instance method name");
    }

    @Test
    void nonComponentClass_doesNotGenerateJson() {
        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.PlainClass",
                """
                package test;

                public class PlainClass {
                    private String name;
                }
                """
        );

        Compilation compilation = compile(source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        boolean hasClassIndex = compilation.generatedFiles().stream()
                .anyMatch(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"));
        assertFalse(hasClassIndex, "Should NOT generate JSON for non-@Component classes");
    }

    @Test
    void componentWithSuperclass_jsonContainsSuperTypes() throws Exception {
        JavaFileObject baseClass = JavaFileObjects.forSourceString(
                "test.BaseService",
                """
                package test;

                public abstract class BaseService {
                    public abstract void execute();
                }
                """
        );

        JavaFileObject source = JavaFileObjects.forSourceString(
                "test.ConcreteService",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class ConcreteService extends BaseService {
                    public ConcreteService() {}

                    @Override
                    public void execute() {}
                }
                """
        );

        Compilation compilation = compile(baseClass, source);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        JavaFileObject jsonFile = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String json = jsonFile.getCharContent(false).toString();
        assertTrue(json.contains("test.ConcreteService"));
        assertTrue(json.contains("test.BaseService"), "JSON should contain superclass");
    }

    @Test
    void multipleComponents_allIncludedInJson() throws Exception {
        JavaFileObject source1 = JavaFileObjects.forSourceString(
                "test.CompA",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class CompA {
                    public CompA() {}
                }
                """
        );

        JavaFileObject source2 = JavaFileObjects.forSourceString(
                "test.CompB",
                """
                package test;

                import vn.io.lcx.common.annotation.Component;

                @Component
                public class CompB {
                    public CompB() {}
                }
                """
        );

        Compilation compilation = compile(source1, source2);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());

        String allJsonContent = compilation.generatedFiles().stream()
                .filter(f -> f.getName().contains("class-index") && f.getName().endsWith(".json"))
                .map(f -> {
                    try {
                        return f.getCharContent(false).toString();
                    } catch (Exception e) {
                        return "";
                    }
                })
                .reduce("", String::concat);

        assertTrue(allJsonContent.contains("test.CompA"), "JSON should contain CompA");
        assertTrue(allJsonContent.contains("test.CompB"), "JSON should contain CompB");
    }
}
