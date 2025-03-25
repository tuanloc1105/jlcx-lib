package vn.com.lcx.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MyCollectionUtils {

    public static <T> List<List<T>> splitListIntoBatches(List<T> inputList, int maxBatchSize, int maxBatches) {
        if (maxBatchSize == 0) {
            maxBatchSize = 50_000;
        }
        if (maxBatches == 0) {
            maxBatches = 8;
        }
        int totalSize = inputList.size();

        // Calculate actual batch count, ensuring it does not exceed maxBatches
        int batchCount = Math.min((int) Math.ceil((double) totalSize / maxBatchSize), maxBatches);
        List<List<T>> batches = new ArrayList<>(batchCount);

        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * maxBatchSize;
            int toIndex = Math.min(fromIndex + maxBatchSize, totalSize);
            batches.add(new ArrayList<>(inputList.subList(fromIndex, toIndex)));
        }

        return batches;
    }

    public static <T> List<List<T>> splitListIntoBatches(List<T> inputList, int maxBatches) {
        if (maxBatches == 0) {
            maxBatches = 8;
        }
        int totalSize = inputList.size();

        // Determine batch size dynamically so all elements are included
        int batchSize = (int) Math.ceil((double) totalSize / maxBatches);

        List<List<T>> batches = new ArrayList<>(maxBatches);

        for (int i = 0; i < maxBatches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, totalSize);
            if (fromIndex < totalSize) {
                batches.add(new ArrayList<>(inputList.subList(fromIndex, toIndex)));
            }
        }

        return batches;
    }

    public static <T> void removeNullElement(final Collection<T> collection) {
        Collection<T> nonNullCollection = collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
        collection.clear();
        collection.addAll(nonNullCollection);
    }

}
