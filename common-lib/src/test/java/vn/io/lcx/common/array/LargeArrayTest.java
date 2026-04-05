package vn.io.lcx.common.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LargeArrayTest {

    @Test
    void constructor_initializesCorrectly() {
        LargeArray<String> array = new LargeArray<>(10);

        assertEquals(10, array.getSize());
        assertNotNull(array.getChunks());
        // 10 elements fit in a single chunk (well below Integer.MAX_VALUE - 8)
        assertEquals(1, array.getChunks().size());
    }

    @Test
    void constructor_zeroSize_noChunks() {
        LargeArray<String> array = new LargeArray<>(0);

        assertEquals(0, array.getSize());
        assertTrue(array.getChunks().isEmpty());
    }

    @Test
    void get_and_set_singleChunk() {
        LargeArray<String> array = new LargeArray<>(5);

        array.set(0, "first");
        array.set(2, "middle");
        array.set(4, "last");

        assertEquals("first", array.get(0));
        assertNull(array.get(1));
        assertEquals("middle", array.get(2));
        assertNull(array.get(3));
        assertEquals("last", array.get(4));
    }

    @Test
    void get_and_set_overwriteValue() {
        LargeArray<Integer> array = new LargeArray<>(3);

        array.set(1, 100);
        assertEquals(100, array.get(1));

        array.set(1, 200);
        assertEquals(200, array.get(1));
    }

    @Test
    void getSize_returnsCorrectSize() {
        LargeArray<Object> array = new LargeArray<>(42);
        assertEquals(42, array.getSize());
    }

    @Test
    void setSize_updatesSize() {
        LargeArray<Object> array = new LargeArray<>(10);
        assertEquals(10, array.getSize());

        array.setSize(20);
        assertEquals(20, array.getSize());
    }

    @Test
    void get_defaultValue_isNull() {
        LargeArray<String> array = new LargeArray<>(3);

        // All slots should be null by default (Object array initialization)
        for (int i = 0; i < 3; i++) {
            assertNull(array.get(i));
        }
    }
}
