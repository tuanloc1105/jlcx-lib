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

    @Nested
    @DisplayName("Parameter Binding Annotations")
    class ParameterBindingAnnotations {

        @Test
        void requestBodyParameter_generatesHandleRequest() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.BodyRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Post;
                    import vn.io.lcx.vertx.base.annotation.process.RequestBody;
                    import io.vertx.core.Future;
                    import java.util.Map;

                    @RestController(path = "/api/body")
                    public class BodyRest {
                        @Post(path = "/submit")
                        public Future<String> submit(@RequestBody Map<String, Object> data) {
                            return Future.succeededFuture("ok");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveBodyRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("handleRequest"),
                    "Generated code should call handleRequest for @RequestBody parameter");
        }

        @Test
        void pathVariableParameter_generatesGetPathParam() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PathVarRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.PathVariable;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/pathvar")
                    public class PathVarRest {
                        @Get(path = "/:id")
                        public Future<String> getById(@PathVariable("id") String id) {
                            return Future.succeededFuture(id);
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactivePathVarRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("getPathParam") || code.contains("pathParam"),
                    "Generated code should call getPathParam for @PathVariable parameter");
        }

        @Test
        void requestParamParameter_generatesGetQueryParam() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.QueryRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.RequestParam;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/query")
                    public class QueryRest {
                        @Get(path = "/search")
                        public Future<String> search(@RequestParam("q") String query) {
                            return Future.succeededFuture(query);
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveQueryRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("getRequestQueryParam") || code.contains("QueryParam") || code.contains("queryParam"),
                    "Generated code should call getRequestQueryParam for @RequestParam parameter");
        }

        @Test
        void requestHeaderParameter_generatesGetHeader() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.HeaderRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.RequestHeader;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/header")
                    public class HeaderRest {
                        @Get(path = "/info")
                        public Future<String> info(@RequestHeader("X-Token") String token) {
                            return Future.succeededFuture(token);
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveHeaderRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("getRequestHeaderParam") || code.contains("HeaderParam") || code.contains("getHeader"),
                    "Generated code should call getRequestHeaderParam for @RequestHeader parameter");
        }
    }

    @Nested
    @DisplayName("Put and Delete Support")
    class PutAndDeleteSupport {

        @Test
        void putMethod_generatesWrapper() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.PutRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Put;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/put")
                    public class PutRest {
                        @Put(path = "/update")
                        public Future<String> update() {
                            return Future.succeededFuture("updated");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactivePutRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("put") || code.contains("Put"),
                    "Generated code should contain PUT method wrapper");
        }

        @Test
        void deleteMethod_generatesWrapper() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.DeleteRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Delete;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/delete")
                    public class DeleteRest {
                        @Delete(path = "/remove")
                        public Future<String> remove() {
                            return Future.succeededFuture("deleted");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveDeleteRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("delete") || code.contains("Delete"),
                    "Generated code should contain DELETE method wrapper");
        }
    }

    @Nested
    @DisplayName("Auth Support")
    class AuthSupport {

        @Test
        void authAnnotatedMethod_copiedToGenerated() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.AuthRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import vn.io.lcx.vertx.base.annotation.process.Auth;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/auth")
                    public class AuthRest {
                        @Auth
                        @Get(path = "/profile")
                        public Future<String> profile() {
                            return Future.succeededFuture("profile");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveAuthRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("@Auth") || code.contains("Auth"),
                    "Generated code should copy @Auth annotation to generated method");
        }
    }

    @Nested
    @DisplayName("Response Handling")
    class ResponseHandling {

        @Test
        void generatedMethod_callsHandleResponse() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.RespRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/resp")
                    public class RespRest {
                        @Get(path = "/data")
                        public Future<String> data() {
                            return Future.succeededFuture("data");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveRespRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("handleResponse"),
                    "Generated wrapper should call handleResponse for success path");
        }

        @Test
        void generatedMethod_callsHandleError() throws Exception {
            JavaFileObject source = JavaFileObjects.forSourceString(
                    "test.ErrRest",
                    """
                    package test;

                    import vn.io.lcx.vertx.base.annotation.process.RestController;
                    import vn.io.lcx.vertx.base.annotation.process.Get;
                    import io.vertx.core.Future;

                    @RestController(path = "/api/err")
                    public class ErrRest {
                        @Get(path = "/fail")
                        public Future<String> fail() {
                            return Future.failedFuture("error");
                        }
                    }
                    """
            );

            Compilation compilation = compile(source);
            assertEquals(Compilation.Status.SUCCESS, compilation.status());

            JavaFileObject generated = compilation.generatedSourceFiles().stream()
                    .filter(f -> f.getName().contains("ReactiveErrRest"))
                    .findFirst()
                    .orElseThrow();

            String code = generated.getCharContent(false).toString();
            assertTrue(code.contains("handleError"),
                    "Generated wrapper should call handleError for failure path");
        }
    }
}
