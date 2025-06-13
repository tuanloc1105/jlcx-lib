package vn.com.lcx.vertx.base.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.vertx.base.annotation.app.ContextHandler;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.annotation.process.APIKey;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Delete;
import vn.com.lcx.vertx.base.annotation.process.Get;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.Put;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.vertx.base.annotation.process.Controller")
public class ControllerProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<TypeElement, List<ExecutableElement>> classMap = new HashMap<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(vn.com.lcx.vertx.base.annotation.process.Controller.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    // Get all methods
                    List<ExecutableElement> allMethodsOfClass = this.processingEnv.getElementUtils().getAllMembers(typeElement).stream()
                            .filter(e -> {
                                boolean elementIsAMethod = e.getKind() == ElementKind.METHOD;
                                boolean isNotStaticAndFinal = !(e.getModifiers().contains(Modifier.FINAL) || e.getModifiers().contains(Modifier.STATIC));
                                boolean notHashCodeMethod = !"hashCode".equalsIgnoreCase(e.getSimpleName().toString());
                                boolean notEqualsMethod = !"equals".equalsIgnoreCase(e.getSimpleName().toString());
                                boolean notToStringMethod = !"toString".equalsIgnoreCase(e.getSimpleName().toString());
                                boolean annotatedWithGetOrPostOrPutOrDelete =
                                        e.getAnnotation(Get.class) != null ||
                                                e.getAnnotation(Post.class) != null ||
                                                e.getAnnotation(Put.class) != null ||
                                                e.getAnnotation(Delete.class) != null;
                                return elementIsAMethod &&
                                        isNotStaticAndFinal &&
                                        notHashCodeMethod &&
                                        notEqualsMethod &&
                                        notToStringMethod &&
                                        annotatedWithGetOrPostOrPutOrDelete;
                            })
                            .map(member -> (ExecutableElement) member).collect(Collectors.toList());
                    classMap.put(typeElement, allMethodsOfClass);
                } catch (Exception e) {
                    this.processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    ExceptionUtils.getStackTrace(e)
                            );
                }
            }
        }
        boolean serveStaticResource = false;
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(VertxApplication.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    VertxApplication vertxApplicationAnnotation = typeElement.getAnnotation(VertxApplication.class);
                    serveStaticResource = vertxApplicationAnnotation.staticResource();
                } catch (Exception e) {
                    this.processingEnv.
                            getMessager().
                            printMessage(
                                    Diagnostic.Kind.ERROR,
                                    ExceptionUtils.getStackTrace(e)
                            );
                }
            }
        }

        boolean applicationHaveAuthentication = false;
        boolean applicationHaveAPIKeyAuthentication = false;

        if (!classMap.isEmpty()) {

            final var contextHandlerOrderAndClassListMap = new TreeMap<Integer, LinkedList<TypeElement>>();

            for (Element contextHandlerElement : roundEnv.getElementsAnnotatedWith(ContextHandler.class)) {
                if (contextHandlerElement instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) contextHandlerElement;
                    ContextHandler contextHandler = typeElement.getAnnotation(ContextHandler.class);
                    var mapElement = contextHandlerOrderAndClassListMap.get(contextHandler.order());
                    if (mapElement == null) {
                        contextHandlerOrderAndClassListMap.put(contextHandler.order(), new LinkedList<>(Collections.singleton(typeElement)));
                    } else {
                        mapElement.addLast(typeElement);
                    }
                }
            }

            int count = 1;

            final List<String> classProperties = new ArrayList<>();
            final List<String> routerConfigures = new ArrayList<>();
            final List<String> constructorParameters = new ArrayList<>();
            final List<String> constructorBody = new ArrayList<>();
            final var routerHandleCodeForFilter = new StringBuilder();

            if (!contextHandlerOrderAndClassListMap.isEmpty()) {
                contextHandlerOrderAndClassListMap.forEach((integer, typeElements) -> {
                    typeElements.forEach(typeElement -> {
                        constructorParameters.add(
                                String.format(
                                        "%s filter%s%d",
                                        typeElement.getQualifiedName() + CommonConstant.EMPTY_STRING,
                                        typeElement.getSimpleName() + CommonConstant.EMPTY_STRING,
                                        integer
                                )
                        );
                        constructorBody.add(
                                String.format(
                                        "this.filter%1$s%2$d = filter%1$s%2$d;",
                                        typeElement.getSimpleName() + CommonConstant.EMPTY_STRING,
                                        integer
                                )
                        );
                        classProperties.add(
                                String.format(
                                        "private final %s filter%s%d",
                                        typeElement.getQualifiedName() + CommonConstant.EMPTY_STRING,
                                        typeElement.getSimpleName() + CommonConstant.EMPTY_STRING,
                                        integer
                                )
                        );
                        routerHandleCodeForFilter.append(
                                String.format(
                                        "\n                    .handler(filter%1$s%2$d::handle)",
                                        typeElement.getSimpleName() + CommonConstant.EMPTY_STRING,
                                        integer
                                )
                        );
                    });
                });
            }

            for (Map.Entry<TypeElement, List<ExecutableElement>> currentClass : classMap.entrySet()) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        vn.com.lcx.common.utils.DateTimeUtils.toUnixMil(vn.com.lcx.common.utils.DateTimeUtils.generateCurrentTimeDefault()) + ": " +
                                String.format(
                                        "Configuring route for controller : %s",
                                        currentClass.getKey().getQualifiedName()
                                )
                );
                constructorParameters.add(
                        String.format(
                                "%s controller%d",
                                currentClass.getKey().getQualifiedName() + CommonConstant.EMPTY_STRING,
                                count
                        )
                );
                constructorBody.add(
                        String.format(
                                "this.controller%1$d = controller%1$d;",
                                count
                        )
                );
                classProperties.add(
                        String.format(
                                "private final %s controller%d",
                                currentClass.getKey().getQualifiedName() + CommonConstant.EMPTY_STRING,
                                count
                        )
                );
                for (ExecutableElement e : currentClass.getValue()) {

                    boolean isAuthMethod = false;
                    boolean isAPIKeyMethod = false;

                    if (e.getAnnotation(Auth.class) != null) {
                        applicationHaveAuthentication = true;
                        isAuthMethod = true;
                    }

                    if (e.getAnnotation(APIKey.class) != null) {
                        applicationHaveAPIKeyAuthentication = true;
                        isAPIKeyMethod = true;
                    }

                    String basePath;

                    if (StringUtils.isNotBlank(currentClass.getKey().getAnnotation(Controller.class).path())) {
                        String controllerPath = currentClass.getKey().getAnnotation(Controller.class).path();
                        if (controllerPath.startsWith("/")) {
                            basePath = controllerPath;
                        } else {
                            basePath = "/" + controllerPath;
                        }
                    } else {
                        basePath = CommonConstant.EMPTY_STRING;
                    }

                    String routerConfigureCode = "";

                    if (e.getAnnotation(Get.class) != null) {
                        String apiPath;
                        if (StringUtils.isNotBlank(e.getAnnotation(Get.class).path())) {
                            String methodPath = e.getAnnotation(Get.class).path();
                            if (methodPath.startsWith("/")) {
                                apiPath = methodPath;
                            } else {
                                apiPath = "/" + methodPath;
                            }
                        } else {
                            apiPath = CommonConstant.EMPTY_STRING;
                        }
                        routerConfigureCode = String.format(
                                "router.get(\"%s\")",
                                basePath + apiPath
                        );
                    }
                    if (e.getAnnotation(Post.class) != null) {
                        String apiPath;
                        if (StringUtils.isNotBlank(e.getAnnotation(Post.class).path())) {
                            String methodPath = e.getAnnotation(Post.class).path();
                            if (methodPath.startsWith("/")) {
                                apiPath = methodPath;
                            } else {
                                apiPath = "/" + methodPath;
                            }
                        } else {
                            apiPath = CommonConstant.EMPTY_STRING;
                        }
                        routerConfigureCode = String.format(
                                "router.post(\"%s\")",
                                basePath + apiPath
                        );
                    }
                    if (e.getAnnotation(Put.class) != null) {
                        String apiPath;
                        if (StringUtils.isNotBlank(e.getAnnotation(Put.class).path())) {
                            String methodPath = e.getAnnotation(Put.class).path();
                            if (methodPath.startsWith("/")) {
                                apiPath = methodPath;
                            } else {
                                apiPath = "/" + methodPath;
                            }
                        } else {
                            apiPath = CommonConstant.EMPTY_STRING;
                        }
                        routerConfigureCode = String.format(
                                "router.put(\"%s\")",
                                basePath + apiPath
                        );
                    }
                    if (e.getAnnotation(Delete.class) != null) {
                        String apiPath;
                        if (StringUtils.isNotBlank(e.getAnnotation(Delete.class).path())) {
                            String methodPath = e.getAnnotation(Delete.class).path();
                            if (methodPath.startsWith("/")) {
                                apiPath = methodPath;
                            } else {
                                apiPath = "/" + methodPath;
                            }
                        } else {
                            apiPath = CommonConstant.EMPTY_STRING;
                        }
                        routerConfigureCode = String.format(
                                "router.delete(\"%s\")",
                                basePath + apiPath
                        );
                    }
                    if (StringUtils.isNotBlank(routerConfigureCode)) {
                        if (isAuthMethod) {
                            routerConfigureCode += "\n                    .handler(authHandler)";
                        }
                        if (isAPIKeyMethod) {
                            routerConfigureCode += "\n                    .handler(this::validateApiKey)";
                        }
                        if (!contextHandlerOrderAndClassListMap.isEmpty()) {
                            routerConfigureCode += routerHandleCodeForFilter.toString();
                        }
                        routerConfigureCode += String.format(
                                "\n                    .handler(this::createUUIDHandler)\n                    .handler(this.controller%d::%s);",
                                count,
                                e.getSimpleName() + CommonConstant.EMPTY_STRING
                        );
                        routerConfigures.add(routerConfigureCode);
                    }
                }
                ++count;
                routerConfigures.add("");
            }
            if (applicationHaveAuthentication) {
                constructorParameters.add("JWTAuth jwtAuth");
                constructorBody.add("this.jwtAuth = jwtAuth;");
            }
            final String constructor = String.format(
                    "\n    public ApplicationVerticle(%s) {\n%s\n    }\n",
                    String.join(",\n                               ", constructorParameters),
                    constructorBody.stream().collect(Collectors.joining("\n       ", "       ", CommonConstant.EMPTY_STRING))
            );
            String codeToWrite = String.format(
                    "package vn.com.lcx.vertx.verticle;\n" +
                            "\n" +
                            "import io.vertx.core.Promise;\n" +
                            "import io.vertx.ext.auth.authentication.TokenCredentials;\n" +
                            "import io.vertx.ext.auth.jwt.JWTAuth;\n" +
                            "import io.vertx.ext.web.RoutingContext;\n" +
                            "import io.vertx.ext.web.handler.BodyHandler;\n" +
                            "import io.vertx.ext.web.handler.JWTAuthHandler;\n" +
                            "import vn.com.lcx.common.annotation.Verticle;\n" +
                            "import vn.com.lcx.common.constant.CommonConstant;\n" +
                            "import vn.com.lcx.common.utils.LogUtils;\n" +
                            "import vn.com.lcx.common.utils.MyStringUtils;\n" +
                            "import vn.com.lcx.vertx.base.config.HttpOption;\n" +
                            "import vn.com.lcx.vertx.base.custom.MyRouter;\n" +
                            "import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;\n" +
                            "\n" +
                            "@Verticle\n" +
                            "public class ApplicationVerticle extends VertxBaseVerticle {\n" +
                            "\n" +
                            "    %s\n" +
                            "%s" +
                            "%s\n" +
                            "    @Override\n" +
                            "    public io.vertx.core.Future<io.vertx.core.http.HttpServer> start() {\n" +
                            "        try {\n" +
                            "%s" +
                            "            io.vertx.ext.web.Router router = MyRouter.router(super.vertx);\n" +
                            "%s\n" +
                            "\n" +
                            "            boolean enableMetric = Boolean.parseBoolean(\n" +
                            "                    CommonConstant.applicationConfig.getPropertyWithEnvironment(\"server.enable-metrics\") + CommonConstant.EMPTY_STRING\n" +
                            "            );\n" +
                            "            if (enableMetric) {\n" +
                            "                router.route(\"/metrics\").handler(io.vertx.micrometer.PrometheusScrapingHandler.create());\n" +
                            "            }\n" +
                            "            router.route().handler(BodyHandler.create());\n" +
                            "\n" +
                            "            router.get(\"/health\").handler(routingContext -> routingContext.response().end(\"OK\"));\n" +
                            "            router.get(\"/starting_probe\").handler(routingContext -> {\n" +
                            "                routingContext.response().end(\"OK\");\n" +
                            "            });\n\n" +
                            "%s\n" +
                            "            final String portString = CommonConstant.applicationConfig.getProperty(\"server.port\");\n" +
                            "            int port;\n" +
                            "            if (MyStringUtils.isNotBlank(portString) && MyStringUtils.isNumeric(portString)) {\n" +
                            "                port = Integer.parseInt(portString);\n" +
                            "            } else {\n" +
                            "                port = 8080;\n" +
                            "            }\n" +
                            "\n" +
                            "            final boolean enableHttp2 = Boolean.parseBoolean(CommonConstant.applicationConfig.getProperty(\"server.enable-http-2\") + CommonConstant.EMPTY_STRING);\n" +
                            "            io.vertx.core.Future<io.vertx.core.http.HttpServer> future;\n" +
                            "            if (enableHttp2) {\n" +
                            "                future = vertx.createHttpServer(HttpOption.configureHttp2H2C(port))\n" +
                            "                        .requestHandler(router)\n" +
                            "                        .listen()\n" +
                            "                        .onSuccess(server -> {\n" +
                            "                            LogUtils.writeLog(LogUtils.Level.INFO, \"HTTP2 server started on port \" + port);\n" +
                            "                        });\n" +
                            "            } else {\n" +
                            "                future = vertx.createHttpServer()\n" +
                            "                        .requestHandler(router)\n" +
                            "                        .listen(port)\n" +
                            "                        .onSuccess(server -> {\n" +
                            "                            LogUtils.writeLog(LogUtils.Level.INFO, \"HTTP server started on port \" + port);\n" +
                            "                        });\n" +
                            "            }\n" +
                            "            return future;\n" +
                            "        } catch (Throwable e) {\n" +
                            "            LogUtils.writeLog(e.getMessage(), e);\n" +
                            "            throw e;\n" +
                            "        }\n" +
                            "    }\n" +
                            "\n" +
                            "%s" +
                            "\n" +
                            "}\n",
                    classProperties.stream().collect(Collectors.joining(";\n    ", CommonConstant.EMPTY_STRING, ";")),
                    applicationHaveAuthentication ? "    private final JWTAuth jwtAuth;\n" : CommonConstant.EMPTY_STRING,
                    constructor,
                    applicationHaveAuthentication ? "            JWTAuthHandler authHandler = JWTAuthHandler.create(this.jwtAuth);\n" : CommonConstant.EMPTY_STRING,
                    serveStaticResource ?
                            "            router.route(\"/*\").handler(io.vertx.ext.web.handler.StaticHandler.create(\"webroot\"));\n" +
                                    "            router.route().last().handler(ctx -> {\n" +
                                    "                ctx.response()\n" +
                                    "                        .putHeader(\"Content-Type\", \"text/html\")\n" +
                                    "                        .sendFile(\"webroot/index.html\");\n" +
                                    "            });\n" :
                            CommonConstant.EMPTY_STRING,
                    routerConfigures.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n            ", "            ", "\n")),
                    applicationHaveAPIKeyAuthentication ?
                            "    public void validateApiKey(RoutingContext context) {\n" +
                                    "        String apiKey = context.request().getHeader(\"x-api-key\");\n" +
                                    "        String validApiKey = CommonConstant.applicationConfig.getProperty(\"server.api-key\");\n" +
                                    "        if (!((apiKey + CommonConstant.EMPTY_STRING).equals(validApiKey))) {\n" +
                                    "            context.response().setStatusCode(401).end(\"Invalid api key\");\n" +
                                    "        } else {\n" +
                                    "            context.next();\n" +
                                    "        }\n" +
                                    "    }\n" :
                            CommonConstant.EMPTY_STRING
            );
            try {
                JavaFileObject builderFile = this.processingEnv.getFiler().createSourceFile("vn.com.lcx.vertx.verticle.ApplicationVerticle");
                try (Writer writer = builderFile.openWriter()) {
                    writer.write(codeToWrite);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

}
