package vn.com.lcx.processor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Delete;
import vn.com.lcx.vertx.base.annotation.process.Get;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.Put;
import vn.com.lcx.vertx.base.annotation.process.RequestBody;
import vn.com.lcx.vertx.base.annotation.process.RestController;
import vn.com.lcx.vertx.base.controller.ReactiveController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("vn.com.lcx.vertx.base.annotation.process.RestController")
public class RestControllerProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RestController.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) annotatedElement;
                try {
                    processRestController(typeElement);
                } catch (Exception e) {
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return true;
    }

    private void processRestController(TypeElement typeElement) throws IOException {
        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
        String originalClassName = typeElement.getSimpleName().toString();
        String generatedClassName = "Reactive" + originalClassName;
        String fullGeneratedClassName = packageName + "." + generatedClassName;

        RestController restController = typeElement.getAnnotation(RestController.class);
        String path = restController.path();

        List<ExecutableElement> methods = typeElement.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD).map(e -> (ExecutableElement) e).filter(e -> e.getAnnotation(Post.class) != null || e.getAnnotation(Get.class) != null || e.getAnnotation(Put.class) != null || e.getAnnotation(Delete.class) != null).collect(Collectors.toList());

        StringBuilder classContent = new StringBuilder();
        classContent.append("package ").append(packageName).append(";\n\n");
        classContent.append("import ").append(Gson.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(TypeToken.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(RoutingContext.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Component.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Controller.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(ReactiveController.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(typeElement.getQualifiedName()).append(";\n");

        // Add imports for method annotations
        classContent.append("import ").append(Post.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Get.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Put.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Delete.class.getCanonicalName()).append(";\n");
        classContent.append("import ").append(Auth.class.getCanonicalName()).append(";\n");

        // Add imports for request/response types if needed (simplified for now, assuming they are imported or fully qualified if complex)
        // Actually, we need to handle imports for types used in methods. 
        // For simplicity, we can use fully qualified names in the generated code or try to import them.
        // Let's rely on the fact that we are in the same package or use fully qualified names where possible, 
        // but for method parameters and return types, we might need imports.
        // A safer bet for a robust processor is to use fully qualified names for everything except standard java.lang types.

        classContent.append("\n");
        classContent.append("@Component\n");
        classContent.append("@Controller(path = \"").append(path).append("\")\n");
        classContent.append("public class ").append(generatedClassName).append(" extends ReactiveController {\n\n");

        String variableName = StringUtils.uncapitalize(originalClassName);
        classContent.append("    private final ").append(originalClassName).append(" ").append(variableName).append(";\n");
        classContent.append("    private final Gson gson;\n\n");

        classContent.append("    public ").append(generatedClassName).append("(").append(originalClassName).append(" ").append(variableName).append(", ").append("Gson gson) {\n");
        classContent.append("        this.").append(variableName).append(" = ").append(variableName).append(";\n");
        classContent.append("        this.gson = gson;\n");
        classContent.append("    }\n\n");

        for (ExecutableElement method : methods) {
            generateMethod(classContent, method, variableName);
        }

        classContent.append("}\n");

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fullGeneratedClassName);
        try (Writer writer = builderFile.openWriter()) {
            writer.write(classContent.toString());
        }
    }

    private void generateMethod(StringBuilder sb, ExecutableElement method, String controllerVarName) {
        // Copy annotations
        Post post = method.getAnnotation(Post.class);
        if (post != null) sb.append("    @Post(path = \"").append(post.path()).append("\")\n");
        Get get = method.getAnnotation(Get.class);
        if (get != null) sb.append("    @Get(path = \"").append(get.path()).append("\")\n");
        Put put = method.getAnnotation(Put.class);
        if (put != null) sb.append("    @Put(path = \"").append(put.path()).append("\")\n");
        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null) sb.append("    @Delete(path = \"").append(delete.path()).append("\")\n");

        if (method.getAnnotation(Auth.class) != null) {
            sb.append("    @Auth\n");
        }

        sb.append("    public void ").append(method.getSimpleName()).append("(RoutingContext ctx) {\n");
        sb.append("        try {\n");

        List<String> args = new ArrayList<>();

        for (VariableElement param : method.getParameters()) {
            String paramName = param.getSimpleName().toString();
            String paramType = param.asType().toString();

            if (param.getAnnotation(RequestBody.class) != null) {
                sb.append("            ").append(paramType).append(" ").append(paramName).append(" = handleRequest(ctx, gson, new TypeToken<>() {\n");
                sb.append("            });\n");
                args.add(paramName);
            } else if (param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.PathVariable.class) != null) {
                vn.com.lcx.vertx.base.annotation.process.PathVariable pathVar = param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.PathVariable.class);
                String pathName = StringUtils.isNotBlank(pathVar.value()) ? pathVar.value() : paramName;
                sb.append("            ").append(paramType).append(" ").append(paramName).append(" = getPathParam(ctx, \"").append(pathName).append("\");\n");
                args.add(paramName);
            } else if (param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestParam.class) != null) {
                vn.com.lcx.vertx.base.annotation.process.RequestParam reqParam = param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestParam.class);
                String queryName = StringUtils.isNotBlank(reqParam.value()) ? reqParam.value() : paramName;
                boolean required = reqParam.required();
                String defaultValue = reqParam.defaultValue();

                if (required) {
                    sb.append("            ").append(paramType).append(" ").append(paramName).append(" = getRequestQueryParam(ctx, \"").append(queryName).append("\");\n");
                } else {
                    // Handle optional/default value.
                    // Since getNoneRequiringRequestQueryParam returns String or null, we might need casting if paramType is not String.
                    // For simplicity, assuming String for now or that the base controller methods handle generic types if implemented that way.
                    // The base controller has: public <T> T getNoneRequiringRequestQueryParam(RoutingContext context, String paramName, Function<String, T> function)
                    // But here we just want the string value or default.
                    // Let's use getNoneRequiringRequestQueryParam(ctx, name) which returns String.
                    sb.append("            ").append(paramType).append(" ").append(paramName).append(" = getNoneRequiringRequestQueryParam(ctx, \"").append(queryName).append("\");\n");
                    if (StringUtils.isNotBlank(defaultValue)) {
                        sb.append("            if (").append(paramName).append(" == null) ").append(paramName).append(" = \"").append(defaultValue).append("\";\n");
                    }
                }
                args.add(paramName);
            } else if (param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestForm.class) != null) {
                vn.com.lcx.vertx.base.annotation.process.RequestForm reqForm = param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestForm.class);
                String formName = StringUtils.isNotBlank(reqForm.value()) ? reqForm.value() : paramName;
                sb.append("            ").append(paramType).append(" ").append(paramName).append(" = getFormParam(ctx, \"").append(formName).append("\");\n");
                args.add(paramName);
            } else if (param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestFile.class) != null) {
                vn.com.lcx.vertx.base.annotation.process.RequestFile reqFile = param.getAnnotation(vn.com.lcx.vertx.base.annotation.process.RequestFile.class);
                String fileName = StringUtils.isNotBlank(reqFile.value()) ? reqFile.value() : paramName;
                sb.append("            ").append(paramType).append(" ").append(paramName).append(" = getFileParam(ctx, \"").append(fileName).append("\");\n");
                args.add(paramName);
            } else if (paramType.equals(RoutingContext.class.getCanonicalName())) {
                args.add("ctx");
            } else {
                args.add("null");
            }
        }

        sb.append("            ").append(controllerVarName).append(".").append(method.getSimpleName()).append("(").append(String.join(", ", args)).append(").onSuccess(it -> {\n");
        sb.append("                handleResponse(ctx, gson, it);\n");
        sb.append("            }).onFailure(err -> {\n");
        sb.append("                handleError(ctx, gson, err);\n");
        sb.append("            });\n");

        sb.append("        } catch (Throwable t) {\n");
        sb.append("            handleError(ctx, gson, t);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
    }
}
