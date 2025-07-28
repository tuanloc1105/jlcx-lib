package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for file operations using Vert.x FileSystem API.
 * Provides reactive file handling methods for create, edit, delete, move, and copy operations.
 */
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new file with the specified content.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path where the file should be created
     * @param content  the content to write to the file
     * @return Future that completes when the file is created
     */
    public static Future<Void> createFile(Vertx vertx, String filePath, String content) {
        FileSystem fs = vertx.fileSystem();
        return fs.writeFile(filePath, Buffer.buffer(content, "UTF-8"))
                .onSuccess(v -> logger.debug("File created successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to create file: {}", filePath, err));
    }

    /**
     * Creates a new file with binary content.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path where the file should be created
     * @param content  the binary content to write to the file
     * @return Future that completes when the file is created
     */
    public static Future<Void> createFile(Vertx vertx, String filePath, Buffer content) {
        FileSystem fs = vertx.fileSystem();
        return fs.writeFile(filePath, content)
                .onSuccess(v -> logger.debug("File created successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to create file: {}", filePath, err));
    }

    /**
     * Creates a new empty file.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path where the file should be created
     * @return Future that completes when the file is created
     */
    public static Future<Void> createEmptyFile(Vertx vertx, String filePath) {
        FileSystem fs = vertx.fileSystem();
        return fs.writeFile(filePath, Buffer.buffer())
                .onSuccess(v -> logger.debug("Empty file created successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to create empty file: {}", filePath, err));
    }

    /**
     * Creates a directory and all necessary parent directories.
     *
     * @param vertx         the Vert.x instance
     * @param dirPath       the path where the directory should be created
     * @param createParents if true, creates parent directories as needed
     * @return Future that completes when the directory is created
     */
    public static Future<Void> createDirectory(Vertx vertx, String dirPath, boolean createParents) {
        FileSystem fs = vertx.fileSystem();
        if (createParents) {
            return fs.mkdirs(dirPath)
                    .onSuccess(v -> logger.debug("Directory created successfully: {}", dirPath))
                    .onFailure(err -> logger.error("Failed to create directory: {}", dirPath, err));
        } else {
            return fs.mkdir(dirPath)
                    .onSuccess(v -> logger.debug("Directory created successfully: {}", dirPath))
                    .onFailure(err -> logger.error("Failed to create directory: {}", dirPath, err));
        }
    }

    /**
     * Reads the content of a file as a string.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to read
     * @return Future containing the file content as a string
     */
    public static Future<String> readFileAsString(Vertx vertx, String filePath) {
        FileSystem fs = vertx.fileSystem();
        return fs.readFile(filePath)
                .map(buffer -> buffer.toString(StandardCharsets.UTF_8))
                .onSuccess(content -> logger.debug("File read successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to read file: {}", filePath, err));
    }

    /**
     * Reads the content of a file as a Buffer.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to read
     * @return Future containing the file content as a Buffer
     */
    public static Future<Buffer> readFileAsBuffer(Vertx vertx, String filePath) {
        FileSystem fs = vertx.fileSystem();
        return fs.readFile(filePath)
                .onSuccess(buffer -> logger.debug("File read successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to read file: {}", filePath, err));
    }

    /**
     * Appends content to an existing file.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to append to
     * @param content  the content to append
     * @return Future that completes when the content is appended
     */
    public static Future<Void> appendToFile(Vertx vertx, String filePath, String content) {
        FileSystem fs = vertx.fileSystem();
        return fs.open(filePath, new OpenOptions().setWrite(true).setAppend(true).setCreate(true))
                .compose(file -> file.write(Buffer.buffer(content, "UTF-8"))
                        .compose(v -> file.close()))
                .onSuccess(v -> logger.debug("Content appended to file: {}", filePath))
                .onFailure(err -> logger.error("Failed to append to file: {}", filePath, err));
    }

    /**
     * Appends binary content to an existing file.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to append to
     * @param content  the binary content to append
     * @return Future that completes when the content is appended
     */
    public static Future<Void> appendToFile(Vertx vertx, String filePath, Buffer content) {
        FileSystem fs = vertx.fileSystem();
        return fs.open(filePath, new OpenOptions().setWrite(true).setAppend(true).setCreate(true))
                .compose(file -> file.write(content)
                        .compose(v -> file.close()))
                .onSuccess(v -> logger.debug("Content appended to file: {}", filePath))
                .onFailure(err -> logger.error("Failed to append to file: {}", filePath, err));
    }

    /**
     * Overwrites the content of an existing file.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to overwrite
     * @param content  the new content
     * @return Future that completes when the file is overwritten
     */
    public static Future<Void> overwriteFile(Vertx vertx, String filePath, String content) {
        FileSystem fs = vertx.fileSystem();
        return fs.writeFile(filePath, Buffer.buffer(content, "UTF-8"))
                .onSuccess(v -> logger.debug("File overwritten successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to overwrite file: {}", filePath, err));
    }

    /**
     * Overwrites the content of an existing file with binary data.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to overwrite
     * @param content  the new binary content
     * @return Future that completes when the file is overwritten
     */
    public static Future<Void> overwriteFile(Vertx vertx, String filePath, Buffer content) {
        FileSystem fs = vertx.fileSystem();
        return fs.writeFile(filePath, content)
                .onSuccess(v -> logger.debug("File overwritten successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to overwrite file: {}", filePath, err));
    }

    /**
     * Deletes a file.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to delete
     * @return Future that completes when the file is deleted
     */
    public static Future<Void> deleteFile(Vertx vertx, String filePath) {
        FileSystem fs = vertx.fileSystem();
        return fs.delete(filePath)
                .onSuccess(v -> logger.debug("File deleted successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to delete file: {}", filePath, err));
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param vertx   the Vert.x instance
     * @param dirPath the path to the directory to delete
     * @return Future that completes when the directory is deleted
     */
    public static Future<Void> deleteDirectory(Vertx vertx, String dirPath) {
        FileSystem fs = vertx.fileSystem();
        return fs.deleteRecursive(dirPath)
                .onSuccess(v -> logger.debug("Directory deleted successfully: {}", dirPath))
                .onFailure(err -> logger.error("Failed to delete directory: {}", dirPath, err));
    }

    /**
     * Moves a file or directory to a new location.
     *
     * @param vertx      the Vert.x instance
     * @param sourcePath the source path
     * @param targetPath the target path
     * @return Future that completes when the move operation is finished
     */
    public static Future<Void> move(Vertx vertx, String sourcePath, String targetPath) {
        FileSystem fs = vertx.fileSystem();
        return fs.move(sourcePath, targetPath)
                .onSuccess(v -> logger.debug("Moved {} to {}", sourcePath, targetPath))
                .onFailure(err -> logger.error("Failed to move {} to {}", sourcePath, targetPath, err));
    }

    /**
     * Copies a file to a new location.
     *
     * @param vertx      the Vert.x instance
     * @param sourcePath the source file path
     * @param targetPath the target file path
     * @return Future that completes when the copy operation is finished
     */
    public static Future<Void> copyFile(Vertx vertx, String sourcePath, String targetPath) {
        FileSystem fs = vertx.fileSystem();
        return fs.copy(sourcePath, targetPath)
                .onSuccess(v -> logger.debug("File copied from {} to {}", sourcePath, targetPath))
                .onFailure(err -> logger.error("Failed to copy file from {} to {}", sourcePath, targetPath, err));
    }

    /**
     * Copies a file to a new location with options.
     *
     * @param vertx      the Vert.x instance
     * @param sourcePath the source file path
     * @param targetPath the target file path
     * @param options    copy options
     * @return Future that completes when the copy operation is finished
     */
    public static Future<Void> copyFile(Vertx vertx, String sourcePath, String targetPath, CopyOptions options) {
        FileSystem fs = vertx.fileSystem();
        return fs.copy(sourcePath, targetPath, options)
                .onSuccess(v -> logger.debug("File copied from {} to {}", sourcePath, targetPath))
                .onFailure(err -> logger.error("Failed to copy file from {} to {}", sourcePath, targetPath, err));
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param vertx the Vert.x instance
     * @param path  the path to check
     * @return Future containing true if the file/directory exists, false otherwise
     */
    public static Future<Boolean> exists(Vertx vertx, String path) {
        FileSystem fs = vertx.fileSystem();
        return fs.exists(path)
                .onSuccess(exists -> logger.debug("Path {} exists: {}", path, exists))
                .onFailure(err -> logger.error("Failed to check existence of path: {}", path, err));
    }

    /**
     * Gets file properties (size, creation time, last modified time, etc.).
     *
     * @param vertx the Vert.x instance
     * @param path  the path to get properties for
     * @return Future containing the file properties
     */
    public static Future<JsonObject> getFileProperties(Vertx vertx, String path) {
        FileSystem fs = vertx.fileSystem();
        return fs.props(path)
                .map(props -> {
                    JsonObject json = new JsonObject();
                    json.put("size", props.size());
                    json.put("creationTime", props.creationTime());
                    json.put("lastAccessTime", props.lastAccessTime());
                    json.put("lastModifiedTime", props.lastModifiedTime());
                    json.put("isDirectory", props.isDirectory());
                    json.put("isRegularFile", props.isRegularFile());
                    json.put("isSymbolicLink", props.isSymbolicLink());
                    json.put("isOther", props.isOther());
                    return json;
                })
                .onSuccess(props -> logger.debug("File properties retrieved for: {}", path))
                .onFailure(err -> logger.error("Failed to get file properties for: {}", path, err));
    }

    /**
     * Lists the contents of a directory.
     *
     * @param vertx the Vert.x instance
     * @param path  the directory path to list
     * @return Future containing the list of file names in the directory
     */
    public static Future<List<String>> listDirectory(Vertx vertx, String path) {
        FileSystem fs = vertx.fileSystem();
        return fs.readDir(path)
                .onSuccess(files -> logger.debug("Directory listed successfully: {} ({} files)", path, files.size()))
                .onFailure(err -> logger.error("Failed to list directory: {}", path, err));
    }

    /**
     * Lists the contents of a directory with a filter.
     *
     * @param vertx  the Vert.x instance
     * @param path   the directory path to list
     * @param filter a filter pattern (e.g., "*.txt")
     * @return Future containing the list of filtered file names in the directory
     */
    public static Future<List<String>> listDirectory(Vertx vertx, String path, String filter) {
        FileSystem fs = vertx.fileSystem();
        return fs.readDir(path, filter)
                .onSuccess(files -> logger.debug("Directory listed successfully: {} ({} files)", path, files.size()))
                .onFailure(err -> logger.error("Failed to list directory: {}", path, err));
    }

    /**
     * Opens a file for reading and writing with custom options.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to open
     * @param options  the open options
     * @return Future containing the AsyncFile instance
     */
    public static Future<AsyncFile> openFile(Vertx vertx, String filePath, OpenOptions options) {
        FileSystem fs = vertx.fileSystem();
        return fs.open(filePath, options)
                .onSuccess(file -> logger.debug("File opened successfully: {}", filePath))
                .onFailure(err -> logger.error("Failed to open file: {}", filePath, err));
    }

    /**
     * Opens a file for reading.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to open
     * @return Future containing the AsyncFile instance
     */
    public static Future<AsyncFile> openFileForReading(Vertx vertx, String filePath) {
        return openFile(vertx, filePath, new OpenOptions().setRead(true));
    }

    /**
     * Opens a file for writing.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to open
     * @return Future containing the AsyncFile instance
     */
    public static Future<AsyncFile> openFileForWriting(Vertx vertx, String filePath) {
        return openFile(vertx, filePath, new OpenOptions().setWrite(true).setCreate(true));
    }

    /**
     * Opens a file for appending.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to open
     * @return Future containing the AsyncFile instance
     */
    public static Future<AsyncFile> openFileForAppending(Vertx vertx, String filePath) {
        return openFile(vertx, filePath, new OpenOptions().setWrite(true).setAppend(true).setCreate(true));
    }

    /**
     * Processes a file line by line using a custom handler.
     *
     * @param vertx    the Vert.x instance
     * @param filePath the path to the file to process
     * @param handler  the function to process each line
     * @return Future that completes when all lines are processed
     */
    public static Future<Void> processFileLineByLine(Vertx vertx, String filePath, Function<String, Future<Void>> handler) {
        return openFileForReading(vertx, filePath)
                .compose(file -> {
                    file.handler(buffer -> {
                        String content = buffer.toString();
                        String[] lines = content.split("\n");
                        for (String line : lines) {
                            if (!line.trim().isEmpty()) {
                                handler.apply(line.trim());
                            }
                        }
                    }).endHandler(v -> file.close());
                    return Future.succeededFuture();
                });
    }

    /**
     * Creates a symbolic link.
     *
     * @param vertx      the Vert.x instance
     * @param linkPath   the path for the symbolic link
     * @param targetPath the target path that the link should point to
     * @return Future that completes when the symbolic link is created
     */
    public static Future<Void> createSymbolicLink(Vertx vertx, String linkPath, String targetPath) {
        FileSystem fs = vertx.fileSystem();
        return fs.link(linkPath, targetPath)
                .onSuccess(v -> logger.debug("Symbolic link created: {} -> {}", linkPath, targetPath))
                .onFailure(err -> logger.error("Failed to create symbolic link: {} -> {}", linkPath, targetPath, err));
    }

    /**
     * Gets the real path of a symbolic link.
     *
     * @param vertx    the Vert.x instance
     * @param linkPath the path of the symbolic link
     * @return Future containing the real path
     */
    public static Future<String> getRealPath(Vertx vertx, String linkPath) {
        FileSystem fs = vertx.fileSystem();
        return fs.readSymlink(linkPath)
                .onSuccess(path -> logger.debug("Real path resolved: {} -> {}", linkPath, path))
                .onFailure(err -> logger.error("Failed to resolve real path: {}", linkPath, err));
    }
}
