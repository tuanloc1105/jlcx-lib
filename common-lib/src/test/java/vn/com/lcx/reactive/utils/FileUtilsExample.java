package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example class demonstrating how to use FileUtils for various file operations.
 */
public class FileUtilsExample {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilsExample.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Run the examples
        runFileOperationsExample(vertx)
                .onSuccess(v -> {
                    logger.info("All file operations completed successfully!");
                    vertx.close();
                })
                .onFailure(err -> {
                    logger.error("File operations failed", err);
                    vertx.close();
                });
    }

    /**
     * Demonstrates various file operations using FileUtils.
     */
    public static Future<Void> runFileOperationsExample(Vertx vertx) {
        String testDir = "test-files";
        String testFile = testDir + "/example.txt";
        String backupFile = testDir + "/backup.txt";
        String movedFile = testDir + "/moved.txt";

        return Future.succeededFuture()
                // Create directory
                .compose(v -> {
                    logger.info("Creating test directory...");
                    return FileUtils.createDirectory(vertx, testDir, true);
                })
                // Create a file with content
                .compose(v -> {
                    logger.info("Creating test file...");
                    return FileUtils.createFile(vertx, testFile, "Hello, this is a test file!\nSecond line of content.");
                })
                // Check if file exists
                .compose(v -> {
                    logger.info("Checking if file exists...");
                    return FileUtils.exists(vertx, testFile);
                })
                .compose(exists -> {
                    logger.info("File exists: {}", exists);
                    return Future.succeededFuture();
                })
                // Read file content
                .compose(v -> {
                    logger.info("Reading file content...");
                    return FileUtils.readFileAsString(vertx, testFile);
                })
                .compose(content -> {
                    logger.info("File content: {}", content);
                    return Future.succeededFuture();
                })
                // Get file properties
                .compose(v -> {
                    logger.info("Getting file properties...");
                    return FileUtils.getFileProperties(vertx, testFile);
                })
                .compose(props -> {
                    logger.info("File properties: {}", props.encodePrettily());
                    return Future.succeededFuture();
                })
                // Append content to file
                .compose(v -> {
                    logger.info("Appending content to file...");
                    return FileUtils.appendToFile(vertx, testFile, "\nAppended line!");
                })
                // Read updated content
                .compose(v -> {
                    logger.info("Reading updated file content...");
                    return FileUtils.readFileAsString(vertx, testFile);
                })
                .compose(content -> {
                    logger.info("Updated file content: {}", content);
                    return Future.succeededFuture();
                })
                // Copy file
                .compose(v -> {
                    logger.info("Copying file...");
                    return FileUtils.copyFile(vertx, testFile, backupFile);
                })
                // List directory contents
                .compose(v -> {
                    logger.info("Listing directory contents...");
                    return FileUtils.listDirectory(vertx, testDir);
                })
                .compose(files -> {
                    logger.info("Directory contents: {}", files);
                    return Future.succeededFuture();
                })
                // Move file
                .compose(v -> {
                    logger.info("Moving file...");
                    return FileUtils.move(vertx, testFile, movedFile);
                })
                // Verify move
                .compose(v -> {
                    logger.info("Verifying file move...");
                    return FileUtils.exists(vertx, movedFile);
                })
                .compose(exists -> {
                    logger.info("Moved file exists: {}", exists);
                    return Future.succeededFuture();
                })
                // Create binary file
                .compose(v -> {
                    logger.info("Creating binary file...");
                    Buffer binaryData = Buffer.buffer("Binary content".getBytes());
                    return FileUtils.createFile(vertx, testDir + "/binary.dat", binaryData);
                })
                // Read binary file
                .compose(v -> {
                    logger.info("Reading binary file...");
                    return FileUtils.readFileAsBuffer(vertx, testDir + "/binary.dat");
                })
                .compose(buffer -> {
                    logger.info("Binary file content: {}", buffer.toString());
                    return Future.succeededFuture();
                })
                // Process file line by line
                .compose(v -> {
                    logger.info("Processing file line by line...");
                    return FileUtils.processFileLineByLine(vertx, movedFile, line -> {
                        logger.info("Processing line: {}", line);
                        return Future.succeededFuture();
                    });
                })
                // Clean up - delete files
                .compose(v -> {
                    logger.info("Cleaning up files...");
                    return FileUtils.deleteFile(vertx, movedFile)
                            .compose(w -> FileUtils.deleteFile(vertx, backupFile))
                            .compose(w -> FileUtils.deleteFile(vertx, testDir + "/binary.dat"));
                })
                // Delete directory
                .compose(v -> {
                    logger.info("Deleting test directory...");
                    return FileUtils.deleteDirectory(vertx, testDir);
                });
    }

    /**
     * Example of working with large files using streaming.
     */
    public static Future<Void> streamLargeFileExample(Vertx vertx) {
        String largeFile = "large-file.txt";

        // Create a large file for demonstration
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Line ").append(i).append(": This is a large file content for streaming demonstration.\n");
        }

        return FileUtils.createFile(vertx, largeFile, largeContent.toString())
                .compose(v -> {
                    logger.info("Large file created, now processing with streaming...");
                    return FileUtils.openFileForReading(vertx, largeFile);
                })
                .compose(file -> {
                    return Future.succeededFuture(file.handler(buffer -> {
                        // Process each buffer chunk
                        String chunk = buffer.toString();
                        logger.debug("Processing chunk: {} bytes", chunk.length());
                    }).endHandler(v -> {
                        logger.info("Finished processing large file");
                        file.close();
                    }));
                })
                .compose(v -> FileUtils.deleteFile(vertx, largeFile));
    }

    /**
     * Example of working with symbolic links.
     */
    public static Future<Void> symbolicLinkExample(Vertx vertx) {
        String originalFile = "original.txt";
        String linkFile = "link.txt";

        return FileUtils.createFile(vertx, originalFile, "Original file content")
                .compose(v -> FileUtils.createSymbolicLink(vertx, linkFile, originalFile))
                .compose(v -> FileUtils.getRealPath(vertx, linkFile))
                .compose(realPath -> {
                    logger.info("Symbolic link {} points to: {}", linkFile, realPath);
                    return Future.succeededFuture();
                })
                .compose(v -> FileUtils.readFileAsString(vertx, linkFile))
                .compose(content -> {
                    logger.info("Content read through symbolic link: {}", content);
                    return Future.succeededFuture();
                })
                .compose(v -> FileUtils.deleteFile(vertx, originalFile))
                .compose(v -> FileUtils.deleteFile(vertx, linkFile));
    }
} 
