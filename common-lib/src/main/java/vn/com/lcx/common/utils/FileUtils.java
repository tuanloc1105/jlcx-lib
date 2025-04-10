package vn.com.lcx.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static vn.com.lcx.common.constant.CommonConstant.EMPTY_STRING;

public final class FileUtils {
    private FileUtils() {
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean writeContentToFile(final String filePath, final String content) {
        try {
            FileWriter writer = new FileWriter(filePath, false); // false indicates overwrite mode
            writer.write(content + System.lineSeparator());
            writer.close();
            // LogUtils.writeLog(LogUtils.Level.INFO, "File overwritten successfully.");
            return true;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e);
            return false;
        }

    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean appendContentToFile(final String filePath, final String content) {
        try {
            FileWriter writer = new FileWriter(filePath, true); // true indicates append mode
            writer.write(content + System.lineSeparator());
            writer.close();
            // LogUtils.writeLog(LogUtils.Level.INFO, "Content appended successfully.");
            return true;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e);
            return false;
        }
    }

    public static String read(String pathOfTheSqlFileToRead) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathOfTheSqlFileToRead));
            String line;

            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e);
        }
        String result = contentBuilder.toString();
        val suffixWillBeRemoved = "\n";
        return MyStringUtils.removeSuffixOfString(result, suffixWillBeRemoved);
    }

    public static List<String> readToList(String pathOfTheSqlFileToRead) {
        val result = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathOfTheSqlFileToRead));
            String line;

            while ((line = reader.readLine()) != null) {
                val lineAfterTrim = line.trim();
                if (StringUtils.isNotBlank(lineAfterTrim)) {
                    result.add(lineAfterTrim);
                }
            }

            reader.close();
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e);
        }
        return result;
    }

    public static String pathJoining(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return String.join(File.separator, input);
    }

    public static String pathJoiningFromRoot(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return File.separator + String.join(File.separator, input);
    }

    public static String pathJoiningWithSlash(String... input) {
        if (input == null || input.length == 0) {
            return EMPTY_STRING;
        }
        return String.join("/", input);
    }

    public static boolean createFolderIfNotExists(String folderPath) {
        File folder = new File(folderPath);

        // Check if the folder exists
        if (!folder.exists()) {
            // Attempt to create the folder
            if (folder.mkdirs()) {
                LogUtils.writeLog(LogUtils.Level.INFO, "Folder created successfully: {}", folderPath);
                return true;
            } else {
                LogUtils.writeLog(LogUtils.Level.INFO, "Failed to create the folder: {}", folderPath);
                return false;
            }
        } else {
            LogUtils.writeLog(LogUtils.Level.INFO, "Folder already exists: {}", folderPath);
            return true;
        }
    }

    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        val folderDeleteSuccessfully = folder.delete();
        if (folder.isDirectory() && folderDeleteSuccessfully) {
            LogUtils.writeLog(LogUtils.Level.INFO, "Deleted: {}", folder.getAbsolutePath());
        }
    }

    public static String encodeFileToBase64(String inputFilePath) throws IOException {
        Path filePath = Paths.get(inputFilePath);
        byte[] fileBytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static byte[] readFileIntoBytes(String inputFilePath) throws IOException {
        Path filePath = Paths.get(inputFilePath);
        return Files.readAllBytes(filePath);
    }

    public static String getFileName(String inputFilePath) {
        Path filePath = Paths.get(inputFilePath);
        return filePath.getFileName().toString();
    }

    public static void changeFilePermission(final String filePath,
                                            SystemUserPermission ownerPermission,
                                            SystemUserPermission groupPermission,
                                            SystemUserPermission otherUserPermission) {
        boolean isWindows = System
                .getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");
        if (isWindows) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(filePath + " does not exist");
        }
        var changeFilePermissionCommand = String.format(
                "chmod %s %s %s",
                file.isDirectory() ? "-R" : EMPTY_STRING,
                "" + ownerPermission.handlePermission() + groupPermission.handlePermission() + otherUserPermission.handlePermission(),
                file.getAbsolutePath()
        );
        ShellCommandRunningUtils.runWithProcessBuilder(changeFilePermissionCommand, CommonConstant.ROOT_DIRECTORY_PROJECT_PATH);
    }

    /**
     * Tạo tệp mới tại đường dẫn được chỉ định.
     *
     * @param filePath đường dẫn tệp cần tạo.
     * @return true nếu tạo thành công, false nếu không.
     */
    public static boolean createFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            if (Files.exists(path)) {
                System.out.println("Tệp đã tồn tại: " + filePath);
                return false;
            }
            Files.createFile(path);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi tạo tệp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tạo thư mục (có thể tạo cả các thư mục cha nếu chưa tồn tại).
     *
     * @param dirPath đường dẫn thư mục cần tạo.
     * @return true nếu tạo thành công, false nếu không.
     */
    public static boolean createDirectory(String dirPath) {
        Path path = Paths.get(dirPath);
        try {
            if (Files.exists(path)) {
                System.out.println("Thư mục đã tồn tại: " + dirPath);
                return false;
            }
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi tạo thư mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa tệp hoặc thư mục được chỉ định. Nếu là thư mục thì xóa cả nội dung bên trong theo đệ quy.
     *
     * @param pathStr đường dẫn của tệp/thư mục cần xóa.
     * @return true nếu xóa thành công, false nếu không.
     */
    public static boolean delete(String pathStr) {
        Path path = Paths.get(pathStr);
        try {
            if (!Files.exists(path)) {
                System.out.println("Đường dẫn không tồn tại: " + pathStr);
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
            System.err.println("Lỗi khi xóa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Copy tệp hoặc thư mục từ đường dẫn nguồn sang đích.
     * Nếu đường dẫn nguồn là thư mục, thao tác copy được thực hiện đệ quy.
     *
     * @param sourcePathStr đường dẫn nguồn.
     * @param destPathStr   đường dẫn đích.
     * @return true nếu copy thành công, false nếu không.
     */
    public static boolean copy(String sourcePathStr, String destPathStr) {
        Path sourcePath = Paths.get(sourcePathStr);
        Path destPath = Paths.get(destPathStr);
        try {
            if (!Files.exists(sourcePath)) {
                System.out.println("Đường dẫn nguồn không tồn tại: " + sourcePathStr);
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
            System.err.println("Lỗi khi copy: " + e.getMessage());
            return false;
        }
    }

    /**
     * Di chuyển (cut) tệp hoặc thư mục từ đường dẫn nguồn sang đích.
     *
     * @param sourcePathStr đường dẫn nguồn.
     * @param destPathStr   đường dẫn đích.
     * @return true nếu di chuyển thành công, false nếu không.
     */
    public static boolean move(String sourcePathStr, String destPathStr) {
        Path sourcePath = Paths.get(sourcePathStr);
        Path destPath = Paths.get(destPathStr);
        try {
            if (!Files.exists(sourcePath)) {
                System.out.println("Đường dẫn nguồn không tồn tại: " + sourcePathStr);
                return false;
            }
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi di chuyển: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đổi tên tệp hoặc thư mục. Lưu ý: tên mới chỉ là tên (không bao gồm đường dẫn)
     * và thao tác sẽ được thực hiện trong cùng thư mục chứa tệp/thư mục đó.
     *
     * @param sourcePathStr đường dẫn của tệp/thư mục cần đổi tên.
     * @param newName       tên mới.
     * @return true nếu đổi tên thành công, false nếu không.
     */
    public static boolean rename(String sourcePathStr, String newName) {
        Path sourcePath = Paths.get(sourcePathStr);
        try {
            if (!Files.exists(sourcePath)) {
                System.out.println("Đường dẫn nguồn không tồn tại: " + sourcePathStr);
                return false;
            }
            Path parentPath = sourcePath.getParent();
            if (parentPath == null) {
                System.out.println("Không thể đổi tên thư mục gốc.");
                return false;
            }
            Path newPath = parentPath.resolve(newName);
            Files.move(sourcePath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi đổi tên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách tên các tệp và thư mục có trong thư mục được chỉ định (không đệ quy).
     *
     * @param dirPathStr đường dẫn thư mục.
     * @return danh sách tên các tệp/thư mục. Nếu đường dẫn không hợp lệ, trả về danh sách rỗng.
     */
    public static List<String> listFiles(String dirPathStr) {
        List<String> list = new ArrayList<>();
        File dir = new File(dirPathStr);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    list.add(file.getName());
                }
            }
        } else {
            System.out.println("Đường dẫn không phải là thư mục hoặc không tồn tại: " + dirPathStr);
        }
        return list;
    }

    /**
     * Đọc nội dung tệp tại đường dẫn được chỉ định và trả về dưới dạng mảng byte.
     *
     * @param filePath đường dẫn của tệp cần đọc.
     * @return mảng byte chứa nội dung của tệp; nếu có lỗi xảy ra, trả về mảng byte rỗng.
     */
    public static byte[] readFileAsBytes(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc tệp: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Ghi dữ liệu dạng text vào tệp. Dù file có đuôi là .dat hay không, dữ liệu sẽ được ghi dưới dạng byte
     * với encoding UTF-8. Nếu file đã tồn tại, nội dung cũ sẽ bị ghi đè.
     *
     * @param filePath đường dẫn tệp cần ghi.
     * @param text     nội dung text cần ghi vào tệp.
     * @return true nếu ghi thành công, false nếu có lỗi xảy ra.
     */
    public static boolean writeTextToFile(String filePath, String text) {
        Path path = Paths.get(filePath);
        try {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi dữ liệu vào tệp: " + e.getMessage());
            return false;
        }
    }

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

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        } else {
            return "";
        }
    }

    public static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        } else {
            return "";
        }
    }

    public static String readResourceFileAsText(ClassLoader classLoader, String fileName) {
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
            val result = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            buffer.close();
            return result;
        } catch (IOException e) {
            LogUtils.writeLog(e.getMessage(), e);
            return null;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SystemUserPermission {
        private boolean readable;
        private boolean writeable;
        private boolean executable;

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
