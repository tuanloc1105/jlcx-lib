package vn.com.lcx;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "list-files", defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES)
public class FileListerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    @Override
    public void execute() throws MojoExecutionException {
        listFilesRecursive(projectBaseDir, "");
    }

    private void listFilesRecursive(File directory, String indent) {
        if (directory == null || !directory.exists()) {
            getLog().warn("Directory does not exist: " + directory);
            return;
        }

        getLog().info("directory is " + directory);

        // File[] files = directory.listFiles();
        // if (files == null) return;

        // for (File file : files) {
        //     getLog().info(indent + (file.isDirectory() ? "[D] " : "[F] ") + file.getName());
        //     if (file.isDirectory()) {
        //         listFilesRecursive(file, indent + "  ");
        //     }
        // }
    }
}
