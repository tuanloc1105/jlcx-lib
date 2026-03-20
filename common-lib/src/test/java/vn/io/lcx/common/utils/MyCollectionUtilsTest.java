package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyCollectionUtilsTest {

    @Test
    void splitListIntoBatches_evenSplit() {
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6);
        // maxBatchSize=2, maxBatches=3 => 3 batches of 2
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 2, 3);

        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2), batches.get(0));
        assertEquals(List.of(3, 4), batches.get(1));
        assertEquals(List.of(5, 6), batches.get(2));
    }

    @Test
    void splitListIntoBatches_unevenSplit() {
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7);
        // maxBatchSize=3, maxBatches=5 => 3 batches: [1,2,3], [4,5,6], [7]
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 3, 5);

        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2, 3), batches.get(0));
        assertEquals(List.of(4, 5, 6), batches.get(1));
        assertEquals(List.of(7), batches.get(2));
    }

    @Test
    void splitListIntoBatches_emptyList() {
        List<String> input = List.of();
        List<List<String>> batches = MyCollectionUtils.splitListIntoBatches(input, 5, 3);

        assertTrue(batches.isEmpty());
    }

    @Test
    void splitListIntoBatches_singleElement() {
        List<String> input = List.of("only");
        List<List<String>> batches = MyCollectionUtils.splitListIntoBatches(input, 5, 3);

        assertEquals(1, batches.size());
        assertEquals(List.of("only"), batches.get(0));
    }

    @Test
    void splitListIntoBatches_maxBatchesLimitsBatchCount() {
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // maxBatchSize=2 would need 5 batches, but maxBatches=3 limits to 3
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 2, 3);

        assertEquals(3, batches.size());
        // Only the first 6 elements are included (3 batches * 2 per batch)
        assertEquals(List.of(1, 2), batches.get(0));
        assertEquals(List.of(3, 4), batches.get(1));
        assertEquals(List.of(5, 6), batches.get(2));
    }

    @Test
    void splitListIntoBatches_defaultValuesWhenZero() {
        List<Integer> input = List.of(1, 2, 3);
        // maxBatchSize=0 defaults to 50000, maxBatches=0 defaults to 8
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 0, 0);

        // With 3 elements and batchSize 50000, all fit in 1 batch
        assertEquals(1, batches.size());
        assertEquals(List.of(1, 2, 3), batches.get(0));
    }

    @Test
    void splitListIntoBatches_twoArgOverload_evenSplit() {
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6);
        // maxBatches=3 => batchSize = ceil(6/3) = 2, so 3 batches of 2
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 3);

        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2), batches.get(0));
        assertEquals(List.of(3, 4), batches.get(1));
        assertEquals(List.of(5, 6), batches.get(2));
    }

    @Test
    void splitListIntoBatches_twoArgOverload_unevenSplit() {
        List<Integer> input = List.of(1, 2, 3, 4, 5);
        // maxBatches=3 => batchSize = ceil(5/3) = 2, so 3 batches: [1,2], [3,4], [5]
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 3);

        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2), batches.get(0));
        assertEquals(List.of(3, 4), batches.get(1));
        assertEquals(List.of(5), batches.get(2));
    }

    @Test
    void splitListIntoBatches_twoArgOverload_moreBatchesThanElements() {
        List<Integer> input = List.of(1, 2);
        // maxBatches=5 => batchSize = ceil(2/5) = 1, only 2 batches have data
        List<List<Integer>> batches = MyCollectionUtils.splitListIntoBatches(input, 5);

        assertEquals(2, batches.size());
        assertEquals(List.of(1), batches.get(0));
        assertEquals(List.of(2), batches.get(1));
    }

    @Test
    void removeNullElement_removesNulls() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null, "c"));
        MyCollectionUtils.removeNullElement(list);

        assertEquals(3, list.size());
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void removeNullElement_noNulls_unchanged() {
        List<String> list = new ArrayList<>(Arrays.asList("x", "y", "z"));
        MyCollectionUtils.removeNullElement(list);

        assertEquals(3, list.size());
        assertEquals(List.of("x", "y", "z"), list);
    }

    @Test
    void removeNullElement_allNulls_becomesEmpty() {
        List<String> list = new ArrayList<>(Arrays.asList(null, null, null));
        MyCollectionUtils.removeNullElement(list);

        assertTrue(list.isEmpty());
    }

    @Test
    void removeNullElement_emptyCollection_unchanged() {
        List<String> list = new ArrayList<>();
        MyCollectionUtils.removeNullElement(list);

        assertTrue(list.isEmpty());
    }
}
