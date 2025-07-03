package vn.com.lcx.common.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsTest {
    private File tempFile;
    private File tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("testFileUtils", ".txt");
        tempDir = Files.createTempDirectory("testFileUtilsDir").toFile();
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) tempFile.delete();
        if (tempDir != null && tempDir.exists()) FileUtils.deleteFolder(tempDir);
    }

    @Test
    public void testWriteAndReadContentToFile() {
        String content = "Hello, World!";
        assertTrue(FileUtils.writeContentToFile(tempFile.getAbsolutePath(), content));
        String read = FileUtils.read(tempFile.getAbsolutePath());
        assertEquals(content, read);
    }

    @Test
    public void testAppendContentToFile() {
        String content1 = "Line1";
        String content2 = "Line2";
        assertTrue(FileUtils.writeContentToFile(tempFile.getAbsolutePath(), content1));
        assertTrue(FileUtils.appendContentToFile(tempFile.getAbsolutePath(), content2));
        List<String> lines = FileUtils.readToList(tempFile.getAbsolutePath());
        assertEquals(2, lines.size());
        assertEquals(content1, lines.get(0));
        assertEquals(content2, lines.get(1));
    }

    @Test
    public void testPathJoining() {
        String joined = FileUtils.pathJoining("a", "b", "c");
        assertTrue(joined.contains(File.separator));
    }

    @Test
    public void testPathJoiningFromRoot() {
        String joined = FileUtils.pathJoiningFromRoot("a", "b");
        assertTrue(joined.startsWith(File.separator));
    }

    @Test
    public void testPathJoiningWithSlash() {
        String joined = FileUtils.pathJoiningWithSlash("a", "b");
        assertEquals("a/b", joined);
    }

    @Test
    public void testCreateAndDeleteFolder() {
        File newDir = new File(tempDir, "subfolder");
        assertTrue(FileUtils.createFolderIfNotExists(newDir.getAbsolutePath()));
        assertTrue(newDir.exists());
        FileUtils.deleteFolder(newDir);
        assertFalse(newDir.exists());
    }

    @Test
    public void testGetFileName() {
        String fileName = FileUtils.getFileName(tempFile.getAbsolutePath());
        assertEquals(tempFile.getName(), fileName);
    }

    @Test
    public void testGetFileExtension() {
        assertEquals("txt", FileUtils.getFileExtension(tempFile));
        assertEquals("txt", FileUtils.getFileExtension(tempFile.getAbsolutePath()));
    }

    @Test
    public void testCheckIfExist() {
        assertTrue(FileUtils.checkIfExist(tempFile.getAbsolutePath()));
        assertFalse(FileUtils.checkIfExist("/non/existent/path/" + System.nanoTime()));
    }

    @Test
    public void testSystemUserPermission() {
        FileUtils.SystemUserPermission perm = new FileUtils.SystemUserPermission(true, false, true);
        assertTrue(perm.isReadable());
        assertFalse(perm.isWriteable());
        assertTrue(perm.isExecutable());
        assertEquals(5, perm.handlePermission());
        perm.setWriteable(true);
        assertTrue(perm.isWriteable());
        assertEquals(7, perm.handlePermission());
    }
} 
