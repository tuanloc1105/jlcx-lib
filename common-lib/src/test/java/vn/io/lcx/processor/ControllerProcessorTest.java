package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ControllerProcessor")
class ControllerProcessorTest {

    private Compilation compile(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ControllerProcessor())
                .compile(sources);
    }

    private JavaFileObject vertxAppSource() {
        return JavaFileObjects.forSourceString(
                "test.TestApp",
                """
                package test;

                import vn.io.lcx.vertx.base.annotation.app.VertxApplication;

                @VertxApplication
                public class TestApp {}
                """
        );
    }

    @Nested
    @DisplayName("Basic Controller")
    class BasicController {

        @Test
        void controllerWithGetMethod_generatesApplicationVerticle() {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.HelloController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/api")
                    public class HelloController {
                        @Get(path = "/hello")
                        public void hello(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            assertTrue(compilation.generatedSourceFiles().stream()
                    .anyMatch(f -> f.getName().contains("ApplicationVerticle")));
        }

        @Test
        void generatedVerticle_containsRouteForGetMethod() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.UserController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/users")
                    public class UserController {
                        @Get(path = "/list")
                        public void listUsers(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/users/list"), "Should contain combined route path /users/list");
            assertTrue(code.contains("GET") || code.contains("get"),
                    "Should contain GET route registration");
        }

        @Test
        void controllerWithPostMethod_generatesRoute() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.PostController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/api")
                    public class PostController {
                        @Post(path = "/create")
                        public void create(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/api/create"), "Should contain route path");
        }

        @Test
        void controllerWithMultipleMethods_generatesAllRoutes() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.CrudController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/items")
                    public class CrudController {
                        @Get(path = "/all")
                        public void getAll(RoutingContext ctx) {}

                        @Post(path = "/add")
                        public void add(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/items/all"), "Should contain GET route");
            assertTrue(code.contains("/items/add"), "Should contain POST route");
        }
    }

    @Nested
    @DisplayName("VertxApplication")
    class VertxApplicationTests {

        @Test
        void vertxApplication_withStaticResource_generatesStaticHandler() throws Exception {
            JavaFileObject app = JavaFileObjects.forSourceString(
                    "test.StaticApp",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.app.VertxApplication;

                    @VertxApplication(staticResource = true)
                    public class StaticApp {}
                    """
            );

            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.MinimalController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller
                    public class MinimalController {
                        @Get(path = "/ping")
                        public void ping(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(app, controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("StaticHandler") || code.contains("staticHandler") || code.contains("static"),
                    "Should contain static resource handler");
        }
    }

    @Nested
    @DisplayName("Generated Structure")
    class GeneratedStructure {

        @Test
        void generatedVerticle_hasCorrectPackage() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.PkgController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller
                    public class PkgController {
                        @Get(path = "/test")
                        public void test(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("package vn.io.lcx.vertx.verticle"),
                    "Generated verticle should be in vn.io.lcx.vertx.verticle package");
            assertTrue(code.contains("class ApplicationVerticle"),
                    "Generated class should be named ApplicationVerticle");
        }

        @Test
        void generatedVerticle_containsControllerDependency() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.DepController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller
                    public class DepController {
                        @Get(path = "/dep")
                        public void dep(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("DepController"),
                    "Generated verticle should reference the controller class");
        }
    }
}
