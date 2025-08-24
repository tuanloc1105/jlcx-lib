package vn.com.lcx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mojo to merge all META-INF/class-index-*.json into one class-index-merged.json.
 */
@Mojo(name = "merge-class-index", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class ClassIndexMergerMojo extends AbstractMojo {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            Path metaInfDir = outputDirectory.toPath().resolve("META-INF");
            if (!Files.exists(metaInfDir)) {
                getLog().info("META-INF not found, skipping.");
                return;
            }

            List<Map<String, Object>> merged = new ArrayList<>();
            Type listType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(metaInfDir, "class-index-*.json")) {
                for (Path path : stream) {
                    getLog().info("Merging: " + path.getFileName());
                    try (Reader reader = Files.newBufferedReader(path)) {
                        List<Map<String, Object>> list = gson.fromJson(reader, listType);
                        if (list != null) merged.addAll(list);
                    }
                }
            }

            // Write merged file
            Path mergedFile = metaInfDir.resolve("class-index-merged.json");
            try (Writer writer = Files.newBufferedWriter(mergedFile)) {
                gson.toJson(merged, writer);
            }

            getLog().info("Merged " + merged.size() + " ClassInfo entries into " + mergedFile);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to merge class index files", e);
        }
    }
}
