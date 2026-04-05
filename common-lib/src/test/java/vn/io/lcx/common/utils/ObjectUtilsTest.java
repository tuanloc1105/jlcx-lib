package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsTest {

    // Simple POJOs for field mapping tests (no Lombok)
    static class SourcePojo {
        private String name;
        private int age;
        private String email;

        public SourcePojo() {
        }

        public SourcePojo(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    static class TargetPojo {
        private String name;
        private int age;
        private String address; // not in source

        public TargetPojo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    // --- mapObjects tests ---

    @Test
    void mapObjects_copiesMatchingFields() {
        SourcePojo source = new SourcePojo("Alice", 30, "alice@test.com");
        TargetPojo target = ObjectUtils.mapObjects(source, TargetPojo.class);

        assertNotNull(target);
        assertEquals("Alice", target.getName());
        assertEquals(30, target.getAge());
        // 'email' exists in source but not in target, should be ignored
        // 'address' exists in target but not in source, should remain null
        assertNull(target.getAddress());
    }

    @Test
    void mapObjects_nullFieldsNotCopied() {
        SourcePojo source = new SourcePojo(null, 25, null);
        TargetPojo target = ObjectUtils.mapObjects(source, TargetPojo.class);

        assertNotNull(target);
        assertNull(target.getName());
        assertEquals(25, target.getAge());
    }

    @Test
    void mapObjects_noMatchingFields_returnsEmptyTarget() {
        SourcePojo source = new SourcePojo("Bob", 40, "bob@test.com");

        // Map to a class with no overlapping fields
        NoMatchPojo target = ObjectUtils.mapObjects(source, NoMatchPojo.class);

        assertNotNull(target);
        assertNull(target.getTitle());
        assertEquals(0.0, target.getSalary());
    }

    static class NoMatchPojo {
        private String title;
        private double salary;

        public NoMatchPojo() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }
    }

    // --- isNullOrEmpty tests ---

    @Test
    void isNullOrEmpty_nullObject_returnsTrue() {
        assertTrue(ObjectUtils.isNullOrEmpty(null));
    }

    @Test
    void isNullOrEmpty_emptyString_returnsTrue() {
        assertTrue(ObjectUtils.isNullOrEmpty(""));
    }

    @Test
    void isNullOrEmpty_blankString_returnsTrue() {
        assertTrue(ObjectUtils.isNullOrEmpty("   "));
    }

    @Test
    void isNullOrEmpty_emptyCollection_returnsTrue() {
        assertTrue(ObjectUtils.isNullOrEmpty(Collections.emptyList()));
        assertTrue(ObjectUtils.isNullOrEmpty(new ArrayList<>()));
    }

    @Test
    void isNullOrEmpty_nonEmpty_returnsFalse() {
        assertFalse(ObjectUtils.isNullOrEmpty("hello"));
        assertFalse(ObjectUtils.isNullOrEmpty(List.of("a")));
        assertFalse(ObjectUtils.isNullOrEmpty(42));
    }

    // --- wrapPrimitive tests ---

    @Test
    void wrapPrimitive_intToInteger() {
        assertEquals(Integer.class, ObjectUtils.wrapPrimitive(int.class));
    }

    @Test
    void wrapPrimitive_allPrimitives() {
        assertEquals(Integer.class, ObjectUtils.wrapPrimitive(int.class));
        assertEquals(Long.class, ObjectUtils.wrapPrimitive(long.class));
        assertEquals(Double.class, ObjectUtils.wrapPrimitive(double.class));
        assertEquals(Float.class, ObjectUtils.wrapPrimitive(float.class));
        assertEquals(Boolean.class, ObjectUtils.wrapPrimitive(boolean.class));
        assertEquals(Character.class, ObjectUtils.wrapPrimitive(char.class));
        assertEquals(Byte.class, ObjectUtils.wrapPrimitive(byte.class));
        assertEquals(Short.class, ObjectUtils.wrapPrimitive(short.class));
        assertEquals(Void.class, ObjectUtils.wrapPrimitive(void.class));
    }

    @Test
    void wrapPrimitive_nonPrimitive_returnsSameClass() {
        assertEquals(String.class, ObjectUtils.wrapPrimitive(String.class));
        assertEquals(Integer.class, ObjectUtils.wrapPrimitive(Integer.class));
    }

    // --- getDefaultValue tests ---

    @Test
    void getDefaultValue_allTypes() {
        assertEquals(false, ObjectUtils.getDefaultValue(boolean.class));
        assertEquals((byte) 0, ObjectUtils.getDefaultValue(byte.class));
        assertEquals((short) 0, ObjectUtils.getDefaultValue(short.class));
        assertEquals(0, ObjectUtils.getDefaultValue(int.class));
        assertEquals(0L, ObjectUtils.getDefaultValue(long.class));
        assertEquals(0.0f, ObjectUtils.getDefaultValue(float.class));
        assertEquals(0.0d, ObjectUtils.getDefaultValue(double.class));
        assertEquals('\u0000', ObjectUtils.getDefaultValue(char.class));
    }

    @Test
    void getDefaultValue_objectType_returnsNull() {
        assertNull(ObjectUtils.getDefaultValue(String.class));
        assertNull(ObjectUtils.getDefaultValue(Integer.class));
        assertNull(ObjectUtils.getDefaultValue(Object.class));
    }
}
