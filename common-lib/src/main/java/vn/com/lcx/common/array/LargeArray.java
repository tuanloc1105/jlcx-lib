package vn.com.lcx.common.array;

import java.util.ArrayList;
import java.util.List;

public class LargeArray<T> {
    private static final int CHUNK_SIZE = Integer.MAX_VALUE - 8; // Slightly less than max
    private final List<T[]> chunks = new ArrayList<>();
    private long size;

    public LargeArray(long size) {
        this.size = size;
        long remaining = size;
        while (remaining > 0) {
            //noinspection unchecked
            chunks.add((T[]) new Object[(int) Math.min(CHUNK_SIZE, remaining)]);
            remaining -= CHUNK_SIZE;
        }
    }

    public List<T[]> getChunks() {
        return chunks;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public T get(long index) {
        int chunkIndex = (int) (index / CHUNK_SIZE);
        int innerIndex = (int) (index % CHUNK_SIZE);
        return chunks.get(chunkIndex)[innerIndex];
    }

    public void set(long index, T value) {
        int chunkIndex = (int) (index / CHUNK_SIZE);
        int innerIndex = (int) (index % CHUNK_SIZE);
        chunks.get(chunkIndex)[innerIndex] = value;
    }
}
