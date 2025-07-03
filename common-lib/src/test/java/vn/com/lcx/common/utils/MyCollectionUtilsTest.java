package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyCollectionUtilsTest {
    @Test
    public void testSplitListIntoBatchesWithMaxBatchSizeAndMaxBatches() {
        List<Integer> input = new ArrayList<>();
        for (int i = 1; i <= 105; i++) input.add(i);
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 20, 6);
        assertEquals(6, batches.size());
        assertEquals(20, batches.get(0).size());
        assertEquals(5, batches.get(5).size());
        assertEquals(101, batches.get(5).get(0));
        assertEquals(Arrays.asList(101, 102, 103, 104, 105), batches.get(5));
    }

    @Test
    public void testSplitListIntoBatchesWithDefaults() {
        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 100; i++) input.add(i);
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 0, 0);
        assertTrue(batches.size() <= 8);
        int total = batches.stream().mapToInt(List::size).sum();
        assertEquals(100, total);
    }

    @Test
    public void testSplitListIntoBatchesMaxBatchesOnly() {
        List<Integer> input = new ArrayList<>();
        for (int i = 0; i < 10; i++) input.add(i);
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 3);
        assertEquals(3, batches.size());
        int total = batches.stream().mapToInt(List::size).sum();
        assertEquals(10, total);
    }

    @Test
    public void testSplitListIntoBatchesMaxBatchesZero() {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 0);
        assertTrue(batches.size() <= 8);
        int total = batches.stream().mapToInt(List::size).sum();
        assertEquals(10, total);
    }

    @Test
    public void testRemoveNullElement() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null, "c"));
        MyCollectionUtils.removeNullElement(list);
        assertEquals(3, list.size());
        assertFalse(list.contains(null));
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    public void testRemoveNullElementAllNulls() {
        List<String> list = new ArrayList<>(Arrays.asList(null, null));
        MyCollectionUtils.removeNullElement(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveNullElementNoNulls() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        MyCollectionUtils.removeNullElement(list);
        assertEquals(Arrays.asList(1, 2, 3), list);
    }
} 
