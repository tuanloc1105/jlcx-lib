package vn.com.lcx.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for common collection operations such as splitting lists into batches and removing null elements.
 * <p>
 * This class provides static methods to:
 * <ul>
 *   <li>Split a list into batches of specified size and/or count</li>
 *   <li>Remove null elements from a collection</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated.
 *
 * @author LCX Team
 * @since 1.0
 */
public final class MyCollectionUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private MyCollectionUtils() {
    }

    /**
     * Splits the input list into batches, each with a maximum size and a maximum number of batches.
     * If maxBatchSize or maxBatches is 0, defaults are used (50,000 and 8, respectively).
     *
     * @param inputList the list to split
     * @param maxBatchSize the maximum size of each batch (0 for default)
     * @param maxBatches the maximum number of batches (0 for default)
     * @param <T> the type of elements in the list
     * @return a list of batches (each batch is a list)
     */
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

    /**
     * Splits the input list into a specified maximum number of batches, distributing elements as evenly as possible.
     * If maxBatches is 0, a default of 8 is used.
     *
     * @param inputList the list to split
     * @param maxBatches the maximum number of batches (0 for default)
     * @param <T> the type of elements in the list
     * @return a list of batches (each batch is a list)
     */
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

    /**
     * Removes all null elements from the given collection.
     *
     * @param collection the collection to clean (modified in place)
     * @param <T> the type of elements in the collection
     */
    public static <T> void removeNullElement(final Collection<T> collection) {
        Collection<T> nonNullCollection = collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
        collection.clear();
        collection.addAll(nonNullCollection);
    }

}
