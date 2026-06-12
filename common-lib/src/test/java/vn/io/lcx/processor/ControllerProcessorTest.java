package vn.io.lcx.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.apache.commons.lang3.StringUtils;
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

    private Compilation compileWithRestController(JavaFileObject... sources) {
        return javac()
                .withProcessors(new RestControllerProcessor(), new ControllerProcessor())
                .compile(sources);
    }

    private Compilation compileWithServiceOrder(JavaFileObject... sources) {
        return javac()
                .withProcessors(new ControllerProcessor(), new RestControllerProcessor())
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

    @Nested
    @DisplayName("HTTP Verb Support")
    class HttpVerbSupport {

        @Test
        void putMethod_generatesRoute() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.PutController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Put;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/api")
                    public class PutController {
                        @Put(path = "/update")
                        public void update(RoutingContext ctx) {}
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
            assertTrue(code.contains("/api/update"), "Should contain route path /api/update");
            assertTrue(code.contains("PUT") || code.contains("put"),
                    "Should contain PUT route registration");
        }

        @Test
        void deleteMethod_generatesRoute() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.DeleteController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Delete;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/api")
                    public class DeleteController {
                        @Delete(path = "/remove")
                        public void remove(RoutingContext ctx) {}
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
            assertTrue(code.contains("/api/remove"), "Should contain route path /api/remove");
            assertTrue(code.contains("DELETE") || code.contains("delete"),
                    "Should contain DELETE route registration");
        }

        @Test
        void allHttpMethods_generatedInSingleController() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.AllVerbsController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import vn.io.lcx.vertx.base.annotation.process.Put;
                    import vn.io.lcx.vertx.base.annotation.process.Delete;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/resources")
                    public class AllVerbsController {
                        @Get(path = "/list")
                        public void list(RoutingContext ctx) {}

                        @Post(path = "/create")
                        public void create(RoutingContext ctx) {}

                        @Put(path = "/update")
                        public void update(RoutingContext ctx) {}

                        @Delete(path = "/remove")
                        public void remove(RoutingContext ctx) {}
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
            assertTrue(code.contains("/resources/list"), "Should contain GET route");
            assertTrue(code.contains("/resources/create"), "Should contain POST route");
            assertTrue(code.contains("/resources/update"), "Should contain PUT route");
            assertTrue(code.contains("/resources/remove"), "Should contain DELETE route");
        }
    }

    @Nested
    @DisplayName("Auth and APIKey")
    class AuthAndAPIKey {

        @Test
        void authAnnotatedMethod_generatesAuthHandler() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.AuthController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.Auth;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/secure")
                    public class AuthController {
                        @Auth
                        @Get(path = "/data")
                        public void secureData(RoutingContext ctx) {}
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
            assertTrue(code.contains("auth") || code.contains("JWTAuth") || code.contains("jwtAuth"),
                    "Generated code should contain auth handler for @Auth annotated method");
        }

        @Test
        void apiKeyAnnotatedMethod_generatesApiKeyValidation() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.ApiKeyController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.APIKey;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/external")
                    public class ApiKeyController {
                        @APIKey
                        @Get(path = "/data")
                        public void externalData(RoutingContext ctx) {}
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
            assertTrue(code.contains("apiKey") || code.contains("APIKey") || code.contains("api_key"),
                    "Generated code should contain API key validation for @APIKey annotated method");
        }
    }

    @Nested
    @DisplayName("Multiple Controllers")
    class MultipleControllers {

        @Test
        void twoControllers_bothRoutesGenerated() throws Exception {
            JavaFileObject controller1 = JavaFileObjects.forSourceString(
                    "test.FirstController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/first")
                    public class FirstController {
                        @Get(path = "/hello")
                        public void hello(RoutingContext ctx) {}
                    }
                    """
            );

            JavaFileObject controller2 = JavaFileObjects.forSourceString(
                    "test.SecondController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/second")
                    public class SecondController {
                        @Post(path = "/submit")
                        public void submit(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), controller1, controller2);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/first/hello"),
                    "Should contain route from first controller");
            assertTrue(code.contains("/second/submit"),
                    "Should contain route from second controller");
        }
    }

    @Nested
    @DisplayName("ContextHandler Support")
    class ContextHandlerSupport {

        @Test
        void contextHandler_includedInVerticle() throws Exception {
            JavaFileObject handler = JavaFileObjects.forSourceString(
                    "test.MyContextHandler",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.app.ContextHandler;
                    import io.vertx.ext.web.RoutingContext;

                    @ContextHandler(order = 1)
                    public class MyContextHandler {
                        public void handle(RoutingContext ctx) {
                            ctx.next();
                        }
                    }
                    """
            );

            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.HandlerTestController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller
                    public class HandlerTestController {
                        @Get(path = "/test")
                        public void test(RoutingContext ctx) {}
                    }
                    """
            );

            Compilation compilation = compile(vertxAppSource(), handler, controller);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("MyContextHandler"),
                    "Generated ApplicationVerticle should reference the ContextHandler class");
        }
    }

    @Nested
    @DisplayName("Round Safety")
    class RoundSafety {

        @Test
        void mixedControllerAndRestController_generatesSingleApplicationVerticleWithBothRoutes() throws Exception {
            JavaFileObject rawController = JavaFileObjects.forSourceString(
                    "test.RawController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/raw")
                    public class RawController {
                        @Get(path = "/ping")
                        public void ping(RoutingContext ctx) {}
                    }
                    """
            );
            JavaFileObject restController = JavaFileObjects.forSourceString(
                    "test.UserRestController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/rest")
                    public class UserRestController {
                        @Get(path = "/list")
                        public Future<String> list() {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compileWithRestController(vertxAppSource(), rawController, restController);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());
            assertEquals(1, compilation.generatedSourceFiles().stream()
                            .filter(f -> f.getName().contains("ApplicationVerticle"))
                            .count(),
                    "Should generate exactly one ApplicationVerticle");

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/raw/ping"), "Should contain raw controller route");
            assertTrue(code.contains("/rest/list"), "Should contain rest controller route");
        }

        @Test
        void serviceOrder_restControllerProcessorStillReceivesRestController() throws Exception {
            JavaFileObject rawController = JavaFileObjects.forSourceString(
                    "test.ServiceOrderRawController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/raw")
                    public class ServiceOrderRawController {
                        @Get(path = "/ping")
                        public void ping(RoutingContext ctx) {}
                    }
                    """
            );
            JavaFileObject restController = JavaFileObjects.forSourceString(
                    "test.ServiceOrderRestController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/rest")
                    public class ServiceOrderRestController {
                        @Get(path = "/list")
                        public Future<String> list() {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compileWithServiceOrder(vertxAppSource(), rawController, restController);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());
            assertTrue(compilation.generatedSourceFiles().stream()
                            .anyMatch(f -> f.getName().contains("ReactiveServiceOrderRestController")),
                    "RestControllerProcessor should generate wrapper when ControllerProcessor runs first");

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/raw/ping"), "Should contain raw controller route");
            assertTrue(code.contains("/rest/list"), "Should contain rest controller route");
        }

        @Test
        void restControllerApiKeyRoute_hasValidateApiKeyHandlerInApplicationVerticle() throws Exception {
            JavaFileObject restController = JavaFileObjects.forSourceString(
                    "test.ExternalRestController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.APIKey;
                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/external")
                    public class ExternalRestController {
                        @APIKey
                        @Get(path = "/data")
                        public Future<String> data() {
                            return Future.succeededFuture("data");
                        }
                    }
                    """
            );

            Compilation compilation = compileWithRestController(vertxAppSource(), restController);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject wrapper = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveExternalRestController"))
                    .findFirst()
                    .orElseThrow();
            assertTrue(wrapper.getCharContent(false).toString().contains("@APIKey"),
                    "Generated wrapper should copy @APIKey");

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertTrue(code.contains("/external/data"), "Should contain rest controller route");
            assertTrue(code.contains("validateApiKey"), "Should attach API key handler");
        }

        @Test
        void contextHandlerNotDuplicatedAcrossRounds() throws Exception {
            JavaFileObject handler = JavaFileObjects.forSourceString(
                    "test.SharedContextHandler",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.app.ContextHandler;
                    import io.vertx.ext.web.RoutingContext;

                    @ContextHandler(order = 1)
                    public class SharedContextHandler {
                        public void handle(RoutingContext ctx) {
                            ctx.next();
                        }
                    }
                    """
            );
            JavaFileObject rawController = JavaFileObjects.forSourceString(
                    "test.RawWithHandlerController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/raw")
                    public class RawWithHandlerController {
                        @Get(path = "/handler")
                        public void handler(RoutingContext ctx) {}
                    }
                    """
            );
            JavaFileObject restController = JavaFileObjects.forSourceString(
                    "test.RestWithHandlerController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/rest")
                    public class RestWithHandlerController {
                        @Get(path = "/handler")
                        public Future<String> handler() {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compileWithRestController(vertxAppSource(), handler, rawController, restController);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject verticle = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ApplicationVerticle"))
                    .findFirst()
                    .orElseThrow();

            String code = verticle.getCharContent(false).toString();
            assertEquals(1, StringUtils.countMatches(code, "private final test.SharedContextHandler filterSharedContextHandler1"),
                    "Context handler field should be generated once");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        void controllerWithEmptyPath_usesMethodPathOnly() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.EmptyPathController",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller
                    public class EmptyPathController {
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
            assertTrue(code.contains("/test"), "Should contain method-level path /test");
        }

        @Test
        void methodWithEmptyPath_usesControllerPathOnly() throws Exception {
            JavaFileObject controller = JavaFileObjects.forSourceString(
                    "test.ControllerPathOnly",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.Controller;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.ext.web.RoutingContext;

                    @Controller(path = "/api")
                    public class ControllerPathOnly {
                        @Get
                        public void root(RoutingContext ctx) {}
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
            assertTrue(code.contains("/api"), "Should contain controller-level path /api");
        }
    }
}
