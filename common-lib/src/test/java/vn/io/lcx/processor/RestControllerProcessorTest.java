package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestControllerProcessor")
class RestControllerProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new RestControllerProcessor())
                .compile(sources);
    }

    @Nested
    @DisplayName("Basic Generation")
    class BasicGeneration {

        @Test
        void restController_generatesReactiveWrapper() {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.UserRestController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/users")
                    public class UserRestController {
                        @Get(path = "/list")
                        public Future<String> list() {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("ReactiveUserRestController")),
                    "Should generate Reactive{Name} wrapper");
        }

        @Test
        void generatedClass_extendsReactiveController() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ItemController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/items")
                    public class ItemController {
                        @Get(path = "/all")
                        public Future<String> getAll() {
                            return Future.succeededFuture("items");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveItemController"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("extends ReactiveController"),
                    "Generated class should extend ReactiveController");
            assertTrue(code.contains("@Component"),
                    "Generated class should be annotated with @Component");
            assertTrue(code.contains("@Controller"),
                    "Generated class should be annotated with @Controller");
        }

        @Test
        void generatedClass_hasControllerFieldAndGson() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.SimpleRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController
                    public class SimpleRest {
                        @Get(path = "/test")
                        public Future<String> test() {
                            return Future.succeededFuture("test");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveSimpleRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("SimpleRest"),
                    "Generated class should reference original controller");
            assertTrue(code.contains("Gson"),
                    "Generated class should have Gson field");
        }
    }

    @Nested
    @DisplayName("HTTP Methods")
    class HttpMethods {

        @Test
        void postMethod_generatesPostRoute() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PostRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import io.vertx.core.Future;

                    @RestController(path = "/api")
                    public class PostRest {
                        @Post(path = "/create")
                        public Future<String> create() {
                            return Future.succeededFuture("created");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactivePostRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("@Post") || code.contains("create"),
                    "Generated class should contain POST method wrapper");
        }

        @Test
        void multipleMethods_allGenerated() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.MultiRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import io.vertx.core.Future;

                    @RestController(path = "/multi")
                    public class MultiRest {
                        @Get(path = "/read")
                        public Future<String> read() {
                            return Future.succeededFuture("data");
                        }

                        @Post(path = "/write")
                        public Future<String> write() {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveMultiRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("read"), "Should contain read method");
            assertTrue(code.contains("write"), "Should contain write method");
        }
    }

    @Nested
    @DisplayName("Path Handling")
    class PathHandling {

        @Test
        void controllerPath_preservedInGenerated() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PathRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/v2")
                    public class PathRest {
                        @Get(path = "/items")
                        public Future<String> items() {
                            return Future.succeededFuture("[]");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactivePathRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("/api/v2"),
                    "Generated @Controller should preserve original path");
        }
    }
}
