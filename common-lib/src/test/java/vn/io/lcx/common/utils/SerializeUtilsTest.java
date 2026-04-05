package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SerializeUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void serialize_and_deserialize_roundTrip() throws IOException, ClassNotFoundException {
        TestData original = new TestData("John", 42);

        String path = tempDir.toString();
        String fileName = "test-object";

        SerializeUtils.serialize(original, path, fileName);

        TestData deserialized = SerializeUtils.deserialize(path, fileName);

        assertNotNull(deserialized);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getAge(), deserialized.getAge());
        assertEquals(original, deserialized);
    }

    /**
     * Simple Serializable class for testing serialization round-trip.
     */
    static class TestData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final int age;

        TestData(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestData testData = (TestData) o;
            return age == testData.age && Objects.equals(name, testData.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
