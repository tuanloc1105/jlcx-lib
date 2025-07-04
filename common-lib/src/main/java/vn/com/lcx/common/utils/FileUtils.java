package vn.com.lcx.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static vn.com.lcx.common.constant.CommonConstant.EMPTY_STRING;

/**
 * Utility class for file and directory operations.
 *
 * <p>This class provides a comprehensive set of static methods for common file operations
 * including reading, writing, copying, moving, deleting files and directories.
 * It also includes utilities for path manipulation, file permissions, and encoding operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>File content reading and writing with proper encoding</li>
 *   <li>Directory creation and deletion with recursive operations</li>
 *   <li>File copying and moving operations</li>
 *   <li>Path manipulation utilities</li>
 *   <li>File permission management (Unix/Linux systems)</li>
 *   <li>Base64 encoding/decoding</li>
 *   <li>Resource file reading</li>
 * </ul>
 *
 * <p>All methods handle exceptions gracefully and return appropriate boolean values
 * or default values when operations fail.</p>
 *
 * @author LCX Team
 * @since 1.0
 */
@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public final class FileUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FileUtils() {
    }

    /**
     * Writes content to a file, overwriting any existing content.
     *
     * <p>This method creates a new file if it doesn't exist, or overwrites the existing file.
     * The content is written with UTF-8 encoding and a system-specific line separator is appended.</p>
     *
     * @param filePath the path to the file to write to
     * @param content  the content to write to the file
     * @return true if the operation was successful, false otherwise
     * @throws IllegalArgumentException if filePath is null or empty
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean writeContentToFile(final String filePath, final String content) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try (FileWriter writer = new FileWriter(filePath, false)) { // false indicates overwrite mode
            writer.write(content + System.lineSeparator());
            return true;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e, LogUtils.Level.DEBUG);
            return false;
        }
    }

    /**
     * Appends content to an existing file.
     *
     * <p>This method appends the specified content to the end of the file.
     * If the file doesn't exist, it will be created. The content is written with UTF-8 encoding
     * and a system-specific line separator is appended.</p>
     *
     * @param filePath the path to the file to append to
     * @param content  the content to append to the file
     * @return true if the operation was successful, false otherwise
     * @throws IllegalArgumentException if filePath is null or empty
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean appendContentToFile(final String filePath, final String content) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try (FileWriter writer = new FileWriter(filePath, true)) { // true indicates append mode
            writer.write(content + System.lineSeparator());
            return true;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e, LogUtils.Level.DEBUG);
            return false;
        }
    }

    /**
     * Reads the entire content of a file as a string.
     *
     * <p>This method reads the file line by line and concatenates all lines with newline characters.
     * The final newline character is removed from the result.</p>
     *
     * @param pathOfTheSqlFileToRead the path to the file to read
     * @return the content of the file as a string, or empty string if an error occurs
     * @throws IllegalArgumentException if pathOfTheSqlFileToRead is null or empty
     */
    public static String read(String pathOfTheSqlFileToRead) {
        if (StringUtils.isBlank(pathOfTheSqlFileToRead)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(pathOfTheSqlFileToRead))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e, LogUtils.Level.DEBUG);
        }

        String result = contentBuilder.toString();
        final var suffixWillBeRemoved = "\n";
        return MyStringUtils.removeSuffixOfString(result, suffixWillBeRemoved);
    }

    /**
     * Reads a file and returns its content as a list of non-empty, trimmed lines.
     *
     * <p>This method reads the file line by line, trims each line, and adds non-empty lines
     * to the result list. Empty lines and lines containing only whitespace are ignored.</p>
     *
     * @param pathOfTheSqlFileToRead the path to the file to read
     * @return a list of non-empty, trimmed lines from the file
     * @throws IllegalArgumentException if pathOfTheSqlFileToRead is null or empty
     */
    public static List<String> readToList(String pathOfTheSqlFileToRead) {
        if (StringUtils.isBlank(pathOfTheSqlFileToRead)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        final var result = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(pathOfTheSqlFileToRead))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final var lineAfterTrim = line.trim();
                if (StringUtils.isNotBlank(lineAfterTrim)) {
                    result.add(lineAfterTrim);
                }
            }
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e, LogUtils.Level.DEBUG);
        }
        return result;
    }

    /**
     * Joins path components using the system-specific file separator.
     *
     * <p>This method joins multiple path components into a single path string using
     * the system-specific file separator (e.g., "/" on Unix/Linux, "\" on Windows).</p>
     *
     * @param input the path components to join
     * @return the joined path string, or empty string if input is null or empty
     */
    public static String pathJoining(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return String.join(File.separator, input);
    }

    /**
     * Joins path components from the root directory using the system-specific file separator.
     *
     * <p>This method joins multiple path components into a single path string starting
     * from the root directory (e.g., "/path/to/file" on Unix/Linux).</p>
     *
     * @param input the path components to join
     * @return the joined path string starting from root, or empty string if input is null or empty
     */
    public static String pathJoiningFromRoot(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return File.separator + String.join(File.separator, input);
    }

    /**
     * Joins path components using forward slash as separator.
     *
     * <p>This method joins multiple path components into a single path string using
     * forward slash ("/") as the separator, regardless of the operating system.</p>
     *
     * @param input the path components to join
     * @return the joined path string using forward slashes, or empty string if input is null or empty
     */
    public static String pathJoiningWithSlash(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return String.join("/", input);
    }

    /**
     * Creates a folder and its parent directories if they don't exist.
     *
     * <p>This method creates the specified folder and all necessary parent directories.
     * If the folder already exists, the method returns true without making any changes.</p>
     *
     * @param folderPath the path of the folder to create
     * @return true if the folder was created successfully or already exists, false otherwise
     * @throws IllegalArgumentException if folderPath is null or empty
     */
    public static boolean createFolderIfNotExists(String folderPath) {
        if (StringUtils.isBlank(folderPath)) {
            throw new IllegalArgumentException("Folder path cannot be null or empty");
        }

        File folder = new File(folderPath);

        // Check if the folder exists
        if (!folder.exists()) {
            // Attempt to create the folder
            if (folder.mkdirs()) {
                LogUtils.writeLog(LogUtils.Level.DEBUG, "Folder created successfully: {}", folderPath);
                return true;
            } else {
                LogUtils.writeLog(LogUtils.Level.DEBUG, "Failed to create the folder: {}", folderPath);
                return false;
            }
        } else {
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Folder already exists: {}", folderPath);
            return true;
        }
    }

    /**
     * Deletes a folder and all its contents recursively.
     *
     * <p>This method recursively deletes all files and subdirectories within the specified folder,
     * then deletes the folder itself. If the folder doesn't exist, the method does nothing.</p>
     *
     * @param folder the folder to delete
     * @throws IllegalArgumentException if folder is null
     */
    public static void deleteFolder(File folder) {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
        }

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        final var folderDeleteSuccessfully = folder.delete();
        if (folder.isDirectory() && folderDeleteSuccessfully) {
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Deleted: {}", folder.getAbsolutePath());
        }
    }

    /**
     * Encodes a file to Base64 string.
     *
     * <p>This method reads the entire file into memory and encodes it as a Base64 string.
     * Use with caution for large files as they will be loaded entirely into memory.</p>
     *
     * @param inputFilePath the path to the file to encode
     * @return the Base64 encoded string representation of the file
     * @throws IOException              if an I/O error occurs while reading the file
     * @throws IllegalArgumentException if inputFilePath is null or empty
     */
    public static String encodeFileToBase64(String inputFilePath) throws IOException {
        if (StringUtils.isBlank(inputFilePath)) {
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }

        Path filePath = Paths.get(inputFilePath);
        byte[] fileBytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * Reads a file and returns its content as a byte array.
     *
     * <p>This method reads the entire file into memory and returns it as a byte array.
     * Use with caution for large files as they will be loaded entirely into memory.</p>
     *
     * @param inputFilePath the path to the file to read
     * @return the file content as a byte array
     * @throws IOException              if an I/O error occurs while reading the file
     * @throws IllegalArgumentException if inputFilePath is null or empty
     */
    public static byte[] readFileIntoBytes(String inputFilePath) throws IOException {
        if (StringUtils.isBlank(inputFilePath)) {
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }

        Path filePath = Paths.get(inputFilePath);
        return Files.readAllBytes(filePath);
    }

    /**
     * Gets the file name from a file path.
     *
     * <p>This method extracts just the file name (without the directory path) from a file path.</p>
     *
     * @param inputFilePath the file path
     * @return the file name, or empty string if the path is invalid
     * @throws IllegalArgumentException if inputFilePath is null or empty
     */
    public static String getFileName(String inputFilePath) {
        if (StringUtils.isBlank(inputFilePath)) {
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }

        Path filePath = Paths.get(inputFilePath);
        return filePath.getFileName().toString();
    }

    /**
     * Changes file permissions using Java's native API.
     *
     * <p>This method uses Java's native {@link Files#setPosixFilePermissions(Path, Set)} API
     * to change file permissions, which is more efficient and secure than using external commands.
     * On Windows systems, this method attempts to set basic file attributes using Windows-specific APIs.</p>
     *
     * @param filePath            the path to the file or directory
     * @param ownerPermission     permissions for the owner
     * @param groupPermission     permissions for the group
     * @param otherUserPermission permissions for other users
     * @return true if the operation was successful, false otherwise
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static boolean changeFilePermission(final String filePath,
                                               SystemUserPermission ownerPermission,
                                               SystemUserPermission groupPermission,
                                               SystemUserPermission otherUserPermission) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        if (ownerPermission == null || groupPermission == null || otherUserPermission == null) {
            throw new IllegalArgumentException("Permission parameters cannot be null");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            LogUtils.writeLog(LogUtils.Level.WARN, "File does not exist: {}", filePath);
            return false;
        }

        try {
            // Check if the file system supports POSIX attributes
            if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
                return changePosixFilePermissions(path, ownerPermission, groupPermission, otherUserPermission);
            } else {
                // Fallback for non-POSIX file systems (like Windows)
                return changeBasicFilePermissions(path, ownerPermission, groupPermission, otherUserPermission);
            }
        } catch (IOException e) {
            LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to change file permissions for {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Changes file permissions using POSIX file permissions API.
     *
     * @param path                the file path
     * @param ownerPermission     owner permissions
     * @param groupPermission     group permissions
     * @param otherUserPermission other user permissions
     * @return true if successful, false otherwise
     */
    private static boolean changePosixFilePermissions(Path path,
                                                      SystemUserPermission ownerPermission,
                                                      SystemUserPermission groupPermission,
                                                      SystemUserPermission otherUserPermission) {
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(
                    buildPosixPermissionString(ownerPermission, groupPermission, otherUserPermission)
            );

            Files.setPosixFilePermissions(path, permissions);
            LogUtils.writeLog(LogUtils.Level.DEBUG, "Successfully changed POSIX permissions for: {}", path);
            return true;
        } catch (IOException e) {
            LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to set POSIX permissions for {}: {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Changes basic file permissions for non-POSIX file systems.
     *
     * @param path                the file path
     * @param ownerPermission     owner permissions
     * @param groupPermission     group permissions
     * @param otherUserPermission other user permissions
     * @return true if successful, false otherwise
     */
    private static boolean changeBasicFilePermissions(Path path,
                                                      SystemUserPermission ownerPermission,
                                                      SystemUserPermission groupPermission,
                                                      SystemUserPermission otherUserPermission) {
        try {
            // For non-POSIX systems, we can only set basic read/write permissions
            // This is mainly for Windows systems
            boolean isReadable = ownerPermission.isReadable() || groupPermission.isReadable() || otherUserPermission.isReadable();
            boolean isWritable = ownerPermission.isWriteable() || groupPermission.isWriteable() || otherUserPermission.isWriteable();

            // Set read-only attribute (inverse of writable)
            if (!isWritable) {
                path.toFile().setReadOnly();
                LogUtils.writeLog(LogUtils.Level.DEBUG, "Set file as read-only: {}", path);
            } else {
                // Make writable if it was read-only
                path.toFile().setWritable(true);
                LogUtils.writeLog(LogUtils.Level.DEBUG, "Set file as writable: {}", path);
            }

            return true;
        } catch (Exception e) {
            LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to set basic permissions for {}: {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Builds a POSIX permission string from permission objects.
     *
     * @param ownerPermission     owner permissions
     * @param groupPermission     group permissions
     * @param otherUserPermission other user permissions
     * @return POSIX permission string (e.g., "rw-r--r--")
     */
    private static String buildPosixPermissionString(SystemUserPermission ownerPermission,
                                                     SystemUserPermission groupPermission,
                                                     SystemUserPermission otherUserPermission) {
        StringBuilder permissionString = new StringBuilder();

        // Owner permissions
        permissionString.append(ownerPermission.isReadable() ? "r" : "-");
        permissionString.append(ownerPermission.isWriteable() ? "w" : "-");
        permissionString.append(ownerPermission.isExecutable() ? "x" : "-");

        // Group permissions
        permissionString.append(groupPermission.isReadable() ? "r" : "-");
        permissionString.append(groupPermission.isWriteable() ? "w" : "-");
        permissionString.append(groupPermission.isExecutable() ? "x" : "-");

        // Other user permissions
        permissionString.append(otherUserPermission.isReadable() ? "r" : "-");
        permissionString.append(otherUserPermission.isWriteable() ? "w" : "-");
        permissionString.append(otherUserPermission.isExecutable() ? "x" : "-");

        return permissionString.toString();
    }

    /**
     * Changes file permissions recursively for directories.
     *
     * <p>This method changes permissions for a directory and all its contents recursively.
     * It uses the same permission settings for all files and subdirectories.</p>
     *
     * @param dirPath             the directory path
     * @param ownerPermission     permissions for the owner
     * @param groupPermission     permissions for the group
     * @param otherUserPermission permissions for other users
     * @return true if the operation was successful for all files, false if any failed
     * @throws IllegalArgumentException if dirPath is null or empty
     */
    public static boolean changeDirectoryPermissionsRecursively(final String dirPath,
                                                                SystemUserPermission ownerPermission,
                                                                SystemUserPermission groupPermission,
                                                                SystemUserPermission otherUserPermission) {
        if (StringUtils.isBlank(dirPath)) {
            throw new IllegalArgumentException("Directory path cannot be null or empty");
        }

        Path path = Paths.get(dirPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            LogUtils.writeLog(LogUtils.Level.WARN, "Directory does not exist or is not a directory: {}", dirPath);
            return false;
        }

        try {
            final boolean[] success = {true};

            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!changeFilePermission(file.toString(), ownerPermission, groupPermission, otherUserPermission)) {
                        success[0] = false;
                        LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to change permissions for file: {}", file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!changeFilePermission(dir.toString(), ownerPermission, groupPermission, otherUserPermission)) {
                        success[0] = false;
                        LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to change permissions for directory: {}", dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            return success[0];
        } catch (IOException e) {
            LogUtils.writeLog(LogUtils.Level.ERROR, "Failed to change directory permissions recursively for {}: {}", dirPath, e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new file at the specified path.
     *
     * <p>This method creates a new empty file. If the file already exists, the method returns false.</p>
     *
     * @param filePath the path where the file should be created
     * @return true if the file was created successfully, false if the file already exists or creation failed
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static boolean createFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        try {
            if (Files.exists(path)) {
                return false;
            }
            Files.createFile(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates a directory and all necessary parent directories.
     *
     * <p>This method creates the specified directory and all parent directories that don't exist.
     * If the directory already exists, the method returns false.</p>
     *
     * @param dirPath the path of the directory to create
     * @return true if the directory was created successfully, false if it already exists or creation failed
     * @throws IllegalArgumentException if dirPath is null or empty
     */
    public static boolean createDirectory(String dirPath) {
        if (StringUtils.isBlank(dirPath)) {
            throw new IllegalArgumentException("Directory path cannot be null or empty");
        }

        Path path = Paths.get(dirPath);
        try {
            if (Files.exists(path)) {
                return false;
            }
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Deletes a file or directory recursively.
     *
     * <p>This method deletes the specified file or directory. If it's a directory, all contents
     * are deleted recursively before the directory itself is deleted.</p>
     *
     * @param pathStr the path of the file or directory to delete
     * @return true if the deletion was successful, false if the path doesn't exist or deletion failed
     * @throws IllegalArgumentException if pathStr is null or empty
     */
    public static boolean delete(String pathStr) {
        if (StringUtils.isBlank(pathStr)) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        Path path = Paths.get(pathStr);
        try {
            if (!Files.exists(path)) {
                return false;
            }
            // Duyệt theo post-order để xóa file trước, sau đó xóa thư mục
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @SuppressWarnings("NullableProblems")
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @SuppressWarnings("NullableProblems")
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Copies a file or directory from source to destination.
     *
     * <p>This method copies the source file or directory to the destination. If the source is a directory,
     * the entire directory structure is copied recursively. If the destination already exists, it will be replaced.</p>
     *
     * @param sourcePathStr the source file or directory path
     * @param destPathStr   the destination path
     * @return true if the copy operation was successful, false if the source doesn't exist or copy failed
     * @throws IllegalArgumentException if sourcePathStr or destPathStr is null or empty
     */
    public static boolean copy(String sourcePathStr, String destPathStr) {
        if (StringUtils.isBlank(sourcePathStr) || StringUtils.isBlank(destPathStr)) {
            throw new IllegalArgumentException("Source and destination paths cannot be null or empty");
        }

        Path sourcePath = Paths.get(sourcePathStr);
        Path destPath = Paths.get(destPathStr);
        try {
            if (!Files.exists(sourcePath)) {
                return false;
            }
            if (Files.isDirectory(sourcePath)) {
                // Sử dụng FileVisitor để copy đệ quy thư mục
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @SuppressWarnings("NullableProblems")
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException {
                        Path targetDir = destPath.resolve(sourcePath.relativize(dir));
                        if (!Files.exists(targetDir)) {
                            Files.createDirectory(targetDir);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @SuppressWarnings("NullableProblems")
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.copy(file, destPath.resolve(sourcePath.relativize(file)),
                                StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // Nếu là tệp thì copy trực tiếp
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Moves a file or directory from source to destination.
     *
     * <p>This method moves the source file or directory to the destination. If the destination already exists,
     * it will be replaced. This operation is equivalent to a "cut and paste" operation.</p>
     *
     * @param sourcePathStr the source file or directory path
     * @param destPathStr   the destination path
     * @return true if the move operation was successful, false if the source doesn't exist or move failed
     * @throws IllegalArgumentException if sourcePathStr or destPathStr is null or empty
     */
    public static boolean move(String sourcePathStr, String destPathStr) {
        if (StringUtils.isBlank(sourcePathStr) || StringUtils.isBlank(destPathStr)) {
            throw new IllegalArgumentException("Source and destination paths cannot be null or empty");
        }

        Path sourcePath = Paths.get(sourcePathStr);
        Path destPath = Paths.get(destPathStr);
        try {
            if (!Files.exists(sourcePath)) {
                return false;
            }
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Renames a file or directory.
     *
     * <p>This method renames a file or directory within the same directory. The new name should not include
     * a path, only the new name for the file or directory.</p>
     *
     * @param sourcePathStr the path of the file or directory to rename
     * @param newName       the new name for the file or directory
     * @return true if the rename operation was successful, false if the source doesn't exist or rename failed
     * @throws IllegalArgumentException if sourcePathStr or newName is null or empty
     */
    public static boolean rename(String sourcePathStr, String newName) {
        if (StringUtils.isBlank(sourcePathStr) || StringUtils.isBlank(newName)) {
            throw new IllegalArgumentException("Source path and new name cannot be null or empty");
        }

        Path sourcePath = Paths.get(sourcePathStr);
        try {
            if (!Files.exists(sourcePath)) {
                return false;
            }
            Path parentPath = sourcePath.getParent();
            if (parentPath == null) {
                return false;
            }
            Path newPath = parentPath.resolve(newName);
            Files.move(sourcePath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Lists the names of files and directories in a directory.
     *
     * <p>This method returns a list of file and directory names in the specified directory.
     * The list is not recursive and only includes immediate children of the directory.</p>
     *
     * @param dirPathStr the path of the directory to list
     * @return a list of file and directory names, or an empty list if the directory doesn't exist or is invalid
     * @throws IllegalArgumentException if dirPathStr is null or empty
     */
    public static List<String> listFiles(String dirPathStr) {
        if (StringUtils.isBlank(dirPathStr)) {
            throw new IllegalArgumentException("Directory path cannot be null or empty");
        }

        List<String> list = new ArrayList<>();
        File dir = new File(dirPathStr);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    list.add(file.getName());
                }
            }
        }
        return list;
    }

    /**
     * Reads a file and returns its content as a byte array.
     *
     * <p>This method reads the entire file into memory and returns it as a byte array.
     * If an error occurs during reading, an empty byte array is returned.</p>
     *
     * @param filePath the path of the file to read
     * @return the file content as a byte array, or empty array if an error occurs
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static byte[] readFileAsBytes(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Writes text content to a file with UTF-8 encoding.
     *
     * <p>This method writes the specified text content to a file using UTF-8 encoding.
     * If the file already exists, its content will be overwritten.</p>
     *
     * @param filePath the path of the file to write to
     * @param text     the text content to write to the file
     * @return true if the write operation was successful, false otherwise
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static boolean writeTextToFile(String filePath, String text) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        try {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if a file is a readable text file.
     *
     * <p>This method checks if the specified path points to a readable text file by verifying:
     * - The file exists
     * - The file is not a directory
     * - The file is readable
     * - The file has a text MIME type</p>
     *
     * @param path the path to check
     * @return true if the path points to a readable text file, false otherwise
     */
    public static boolean isReadableTextFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File file = new File(path);

        // Check if the file exists, is a file (not a directory), and is readable
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return false;
        }

        // Check if the file is a text file by inspecting its MIME type
        try {
            String mimeType = Files.probeContentType(Paths.get(path));
            return mimeType != null && mimeType.startsWith("text");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if a file or directory exists at the specified path.
     *
     * @param path the path to check
     * @return true if the file or directory exists, false otherwise
     * @throws IllegalArgumentException if path is null
     */
    public static boolean checkIfExist(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        File file = new File(path);
        return file.exists();
    }

    /**
     * Gets the file extension from a File object.
     *
     * <p>This method extracts the file extension (the part after the last dot) from a file name.
     * If there is no extension or the file name ends with a dot, an empty string is returned.</p>
     *
     * @param file the File object to get the extension from
     * @return the file extension (without the dot), or empty string if no extension exists
     * @throws IllegalArgumentException if file is null
     */
    public static String getFileExtension(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        } else {
            return "";
        }
    }

    /**
     * Gets the file extension from a file path string.
     *
     * <p>This method extracts the file extension (the part after the last dot) from a file path.
     * If there is no extension or the path ends with a dot, an empty string is returned.</p>
     *
     * @param filePath the file path to get the extension from
     * @return the file extension (without the dot), or empty string if no extension exists
     * @throws IllegalArgumentException if filePath is null
     */
    public static String getFileExtension(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        } else {
            return "";
        }
    }

    /**
     * Reads a resource file from the classpath as text.
     *
     * <p>This method reads a resource file from the classpath using the specified ClassLoader.
     * The file content is read as UTF-8 encoded text.</p>
     *
     * @param classLoader the ClassLoader to use for loading the resource
     * @param fileName    the name of the resource file to read
     * @return the content of the resource file as a string, or null if the file is not found or an error occurs
     * @throws IllegalArgumentException if classLoader or fileName is null
     */
    public static String readResourceFileAsText(ClassLoader classLoader, String fileName) {
        if (classLoader == null) {
            throw new IllegalArgumentException("ClassLoader cannot be null");
        }
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        // Get the resource file as an InputStream from the class loader.
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                // File not found in resources
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192]; // Use a reasonable buffer size
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            final var result = buffer.toString(StandardCharsets.UTF_8);
            buffer.close();
            return result;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e, LogUtils.Level.DEBUG);
            return null;
        }
    }

    /**
     * Represents file permissions for different user categories.
     *
     * <p>This class encapsulates read, write, and execute permissions for a file or directory.
     * It provides methods to convert these permissions to the numeric format used by Unix/Linux systems.</p>
     */
    public static class SystemUserPermission {
        /**
         * Whether the user can read the file/directory
         */
        private boolean readable;
        /**
         * Whether the user can write to the file/directory
         */
        private boolean writeable;
        /**
         * Whether the user can execute the file or access the directory
         */
        private boolean executable;

        public SystemUserPermission() {
        }

        public SystemUserPermission(boolean readable, boolean writeable, boolean executable) {
            this.readable = readable;
            this.writeable = writeable;
            this.executable = executable;
        }

        public boolean isReadable() {
            return readable;
        }

        public void setReadable(boolean readable) {
            this.readable = readable;
        }

        public boolean isWriteable() {
            return writeable;
        }

        public void setWriteable(boolean writeable) {
            this.writeable = writeable;
        }

        public boolean isExecutable() {
            return executable;
        }

        public void setExecutable(boolean executable) {
            this.executable = executable;
        }

        /**
         * Converts the permissions to a numeric value.
         *
         * <p>This method converts the boolean permissions to a numeric value where:
         * - Read permission = 4
         * - Write permission = 2
         * - Execute permission = 1</p>
         *
         * @return the numeric representation of the permissions
         */
        public int handlePermission() {
            var result = 0;
            if (this.readable) {
                result += 4;
            }
            if (this.writeable) {
                result += 2;
            }
            if (this.executable) {
                result++;
            }
            return result;
        }
    }
}
