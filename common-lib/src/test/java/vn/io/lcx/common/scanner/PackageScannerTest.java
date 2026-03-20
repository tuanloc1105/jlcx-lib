package vn.io.lcx.common.scanner;

import org.junit.jupiter.api.Test;
import vn.io.lcx.common.config.BuildGson;
import vn.io.lcx.common.config.BuildObjectMapper;
import vn.io.lcx.common.config.ClassPool;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageScannerTest {

    @Test
    void findClasses_existingPackage_returnsClasses() {
        // Scan a known package that definitely has classes on the classpath
        List<Class<?>> classes = PackageScanner.findClasses("vn.io.lcx.common.scanner");

        assertNotNull(classes);
        assertFalse(classes.isEmpty(), "Should find at least PackageScanner itself");
        assertTrue(classes.contains(PackageScanner.class),
                "Should find PackageScanner in its own package");
    }

    @Test
    void findClasses_nonExistentPackage_returnsEmpty() {
        List<Class<?>> classes = PackageScanner.findClasses("com.nonexistent.fake.package.xyz");

        assertNotNull(classes);
        assertTrue(classes.isEmpty(), "Non-existent package should return empty list");
    }

    @Test
    void findClasses_includesSubPackages() {
        // Scan parent package "vn.io.lcx.common.config" which should find classes
        // in the config package itself
        List<Class<?>> classes = PackageScanner.findClasses("vn.io.lcx.common.config");

        assertNotNull(classes);
        assertFalse(classes.isEmpty(), "Should find classes in the config package");
        assertTrue(classes.contains(ClassPool.class),
                "Should find ClassPool in the config package");
        assertTrue(classes.contains(BuildGson.class),
                "Should find BuildGson in the config package");
        assertTrue(classes.contains(BuildObjectMapper.class),
                "Should find BuildObjectMapper in the config package");
    }
}
