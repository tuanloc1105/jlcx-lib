package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void writeContentToFile_createsFile() {
        String filePath = tempDir.resolve("test-write.txt").toString();
        boolean result = FileUtils.writeContentToFile(filePath, "Hello World");
        assertTrue(result);
        assertTrue(new File(filePath).exists());

        String content = FileUtils.read(filePath);
        assertEquals("Hello World", content);
    }

    @Test
    void appendContentToFile_appendsContent() {
        String filePath = tempDir.resolve("test-append.txt").toString();
        FileUtils.writeContentToFile(filePath, "Line 1");
        FileUtils.appendContentToFile(filePath, "Line 2");

        String content = FileUtils.read(filePath);
        assertTrue(content.contains("Line 1"));
        assertTrue(content.contains("Line 2"));
    }

    @Test
    void read_existingFile_returnsContent() throws IOException {
        Path file = tempDir.resolve("test-read.txt");
        Files.writeString(file, "Test content here");

        String content = FileUtils.read(file.toString());
        assertEquals("Test content here", content);
    }

    @Test
    void readToList_multiLineFile_returnsList() {
        String filePath = tempDir.resolve("test-readlist.txt").toString();
        FileUtils.writeContentToFile(filePath, "Line A");
        FileUtils.appendContentToFile(filePath, "Line B");
        FileUtils.appendContentToFile(filePath, "Line C");

        List<String> lines = FileUtils.readToList(filePath);
        assertEquals(3, lines.size());
        assertEquals("Line A", lines.get(0));
        assertEquals("Line B", lines.get(1));
        assertEquals("Line C", lines.get(2));
    }

    @Test
    void readAsBytes_returnsByteArray() throws IOException {
        Path file = tempDir.resolve("test-bytes.txt");
        byte[] data = "Binary content".getBytes();
        Files.write(file, data);

        byte[] result = FileUtils.readFileAsBytes(file.toString());
        assertArrayEquals(data, result);
    }

    @Test
    void readFileIntoBytes_returnsByteArray() throws IOException {
        Path file = tempDir.resolve("test-into-bytes.txt");
        byte[] data = "Some bytes".getBytes();
        Files.write(file, data);

        byte[] result = FileUtils.readFileIntoBytes(file.toString());
        assertArrayEquals(data, result);
    }

    @Test
    void pathJoining_combinesPaths() {
        String result = FileUtils.pathJoining("path", "to", "file.txt");
        String expected = "path" + File.separator + "to" + File.separator + "file.txt";
        assertEquals(expected, result);

        // Null or empty input
        assertEquals("", FileUtils.pathJoining());
        assertEquals("", FileUtils.pathJoining((String[]) null));
    }

    @Test
    void getFileExtension_returnsExtension() {
        assertEquals("txt", FileUtils.getFileExtension("file.txt"));
        assertEquals("java", FileUtils.getFileExtension("MyClass.java"));
        assertEquals("gz", FileUtils.getFileExtension("archive.tar.gz"));
        assertEquals("", FileUtils.getFileExtension("noextension"));

        // File object variant
        assertEquals("txt", FileUtils.getFileExtension(new File("test.txt")));
        assertEquals("", FileUtils.getFileExtension(new File("noext")));
    }

    @Test
    void createDirectoryIfNotExists_createsDir() {
        String dirPath = tempDir.resolve("new-dir").toString();
        assertFalse(new File(dirPath).exists());

        boolean result = FileUtils.createFolderIfNotExists(dirPath);
        assertTrue(result);
        assertTrue(new File(dirPath).exists());
        assertTrue(new File(dirPath).isDirectory());

        // Second call should also return true (already exists)
        assertTrue(FileUtils.createFolderIfNotExists(dirPath));
    }

    @Test
    void createDirectory_createsDir() {
        String dirPath = tempDir.resolve("another-dir").toString();
        boolean result = FileUtils.createDirectory(dirPath);
        assertTrue(result);
        assertTrue(new File(dirPath).isDirectory());

        // Second call returns false (already exists)
        assertFalse(FileUtils.createDirectory(dirPath));
    }

    @Test
    void deleteFile_removesFile() throws IOException {
        Path file = tempDir.resolve("to-delete.txt");
        Files.writeString(file, "delete me");
        assertTrue(Files.exists(file));

        boolean result = FileUtils.delete(file.toString());
        assertTrue(result);
        assertFalse(Files.exists(file));
    }

    @Test
    void moveFile_movesFile() throws IOException {
        Path source = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        Files.writeString(source, "move me");

        boolean result = FileUtils.move(source.toString(), dest.toString());
        assertTrue(result);
        assertFalse(Files.exists(source));
        assertTrue(Files.exists(dest));
        assertEquals("move me", Files.readString(dest));
    }

    @Test
    void copyFile_copiesFile() throws IOException {
        Path source = tempDir.resolve("original.txt");
        Path dest = tempDir.resolve("copied.txt");
        Files.writeString(source, "copy me");

        boolean result = FileUtils.copy(source.toString(), dest.toString());
        assertTrue(result);
        assertTrue(Files.exists(source), "Source should still exist");
        assertTrue(Files.exists(dest), "Destination should exist");
        assertEquals("copy me", Files.readString(dest));
    }

    @Test
    void fileExists_existingFile_returnsTrue() throws IOException {
        Path file = tempDir.resolve("exists.txt");
        Files.writeString(file, "I exist");

        assertTrue(FileUtils.checkIfExist(file.toString()));
        assertFalse(FileUtils.checkIfExist(tempDir.resolve("nonexistent.txt").toString()));
    }

    @Test
    void encodeFileToBase64_and_decode_roundTrip() throws IOException {
        Path file = tempDir.resolve("base64test.txt");
        String originalContent = "Hello Base64 Encoding!";
        Files.writeString(file, originalContent);

        String base64 = FileUtils.encodeFileToBase64(file.toString());
        assertNotNull(base64);
        assertFalse(base64.isEmpty());

        // Decode and verify
        byte[] decoded = Base64.getDecoder().decode(base64);
        String decodedContent = new String(decoded);
        assertEquals(originalContent, decodedContent);
    }

    @Test
    void renameFile_renames() throws IOException {
        Path file = tempDir.resolve("old-name.txt");
        Files.writeString(file, "rename me");

        boolean result = FileUtils.rename(file.toString(), "new-name.txt");
        assertTrue(result);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(tempDir.resolve("new-name.txt")));
        assertEquals("rename me", Files.readString(tempDir.resolve("new-name.txt")));
    }
}
