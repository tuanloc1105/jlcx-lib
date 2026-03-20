package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilsTest {

    @Test
    void generateRandomString_correctLength() {
        String result = RandomUtils.generateRandomString(10);
        assertEquals(10, result.length());

        String empty = RandomUtils.generateRandomString(0);
        assertEquals(0, empty.length());

        String large = RandomUtils.generateRandomString(100);
        assertEquals(100, large.length());
    }

    @Test
    void generateRandomString_alphanumericOnly() {
        String result = RandomUtils.generateRandomString(1000);
        assertTrue(result.matches("[A-Za-z0-9]+"),
                "Generated string should contain only alphanumeric characters");
    }

    @Test
    void generateRandomString_negativeLengthThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> RandomUtils.generateRandomString(-1));
    }

    @Test
    void getRandomNumber_withinRange() {
        for (int i = 0; i < 100; i++) {
            int result = RandomUtils.getRandomNumber(5, 10);
            assertTrue(result >= 5 && result <= 10,
                    "Random number " + result + " should be between 5 and 10");
        }

        // Test when min equals max
        int sameValue = RandomUtils.getRandomNumber(7, 7);
        assertEquals(7, sameValue);
    }

    @Test
    void getRandomNumber_minGreaterThanMaxThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> RandomUtils.getRandomNumber(10, 5));
    }

    @Test
    void getRandomElementAndRemove_removesElement() {
        List<String> list = new ArrayList<>(List.of("A", "B", "C", "D"));
        int originalSize = list.size();

        String removed = RandomUtils.getRandomElementAndRemove(list);

        assertEquals(originalSize - 1, list.size());
        assertFalse(list.contains(removed), "Removed element should no longer be in the list");
        assertTrue(List.of("A", "B", "C", "D").contains(removed),
                "Removed element should be one of the original elements");
    }

    @Test
    void getRandomElementAndRemove_emptyListThrows() {
        List<String> emptyList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> RandomUtils.getRandomElementAndRemove(emptyList));
    }

    @Test
    void shuffleList_sameElements() {
        List<Integer> original = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> toShuffle = new ArrayList<>(original);

        RandomUtils.shuffleList(toShuffle);

        assertEquals(original.size(), toShuffle.size());
        // All elements should still be present
        Set<Integer> originalSet = new HashSet<>(original);
        Set<Integer> shuffledSet = new HashSet<>(toShuffle);
        assertEquals(originalSet, shuffledSet);
    }
}
