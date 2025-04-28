package vn.com.lcx;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JavaAnnotationUpdater {

    public static void replaceGeneratedAnnotationInJavaFiles(String directoryPath) throws IOException {
        Path start = Paths.get(directoryPath);
        try (Stream<Path> paths = Files.walk(start)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> isTextReadable(p, 1024))
                    .forEach(JavaAnnotationUpdater::processFile);
        }
    }

    private static boolean isTextReadable(Path file, int chunkSize) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            char[] buffer = new char[chunkSize];
            return reader.read(buffer) >= 0;
        } catch (IOException e) {
            return false;
        }
    }

    private static void processFile(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String updated = content.replace(
                    "@javax.annotation.Generated",
                    "@javax.annotation.processing.Generated"
            );
            Files.writeString(file, updated, StandardCharsets.UTF_8);
            System.out.println("Processed file: " + file);
        } catch (IOException e) {
            System.err.println("Error processing " + file + ": " + e.getMessage());
        }
    }

}
