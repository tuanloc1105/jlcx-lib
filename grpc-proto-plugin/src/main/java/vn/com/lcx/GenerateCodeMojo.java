package vn.com.lcx;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.ShellCommandRunningUtils;

import java.io.File;
import java.util.Collections;

@Mojo(name = "generate-code", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES)
public class GenerateCodeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    @Parameter(property = "protocFilePath", required = false)
    private String protocFilePath;

    @Parameter(property = "protocGenGrpcJavaFilePath", required = false)
    private String protocGenGrpcJavaFilePath;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final var protoFolderPath = FileUtils.pathJoining(projectBaseDir.getAbsolutePath(), "src", "proto");
        final var protoFolder = new File(protoFolderPath);
        if (!protoFolder.exists()) {
            getLog().info(
                    String.format(
                            "There is no proto file in project, make sure your proto file is placing in %s",
                            protoFolderPath
                    )
            );
            return;
        }
        final var protoFilePaths = FileUtils.listFiles(protoFolderPath);
        final var outputGrpcCodeFolderPath = FileUtils.pathJoining(projectBaseDir.getAbsolutePath(), "target", "grpc-generated-sources", "annotations");
        FileUtils.createFolderIfNotExists(outputGrpcCodeFolderPath);
        if (StringUtils.isNotBlank(protocFilePath) && StringUtils.isNotBlank(protocGenGrpcJavaFilePath)) {
            for (String protoFilePath : protoFilePaths) {
                var oldPathValue = System.getenv("PATH");
                final var result = ShellCommandRunningUtils.runWithProcessBuilder(
                        String.format(
                                "protoc -I=. --java_out=%2$s --grpc-java_out=%2$s %1$s",
                                protoFilePath,
                                outputGrpcCodeFolderPath
                        ),
                        protoFolderPath,
                        5,
                        Collections.singletonList(
                                new ShellCommandRunningUtils.ProcessEnvironment(
                                        "PATH",
                                        String.format("%s:%s:%s", oldPathValue, protocFilePath, protocGenGrpcJavaFilePath)
                                )
                        ),
                        false
                );
                LogUtils.writeLog(LogUtils.Level.INFO, "Result " + result.getExitCode());
            }
        } else {
            for (String protoFilePath : protoFilePaths) {
                final var result = ShellCommandRunningUtils.runWithProcessBuilder(
                        String.format(
                                "protoc -I=. --java_out=%2$s --grpc-java_out=%2$s %1$s",
                                protoFilePath,
                                outputGrpcCodeFolderPath
                        ),
                        protoFolderPath,
                        5,
                        null,
                        false
                );
                LogUtils.writeLog(LogUtils.Level.INFO, "Result " + result.getExitCode());
            }
        }
        project.addCompileSourceRoot(outputGrpcCodeFolderPath);
    }

}
