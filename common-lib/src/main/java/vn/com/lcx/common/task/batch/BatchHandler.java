package vn.com.lcx.common.task.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BatchHandler {

    public <T> void processListInBatches(List<T> inputList, Consumer<List<T>> handlerFunction, int batchSize) {
        final var tempList = new ArrayList<T>();
        for (T t : inputList) {
            if (tempList.size() == batchSize) {
                handlerFunction.accept(new ArrayList<>(tempList));
                tempList.clear();
            }
            tempList.add(t);
        }
        if (!tempList.isEmpty()) {
            handlerFunction.accept(new ArrayList<>(tempList));
            tempList.clear();
        }
    }

}
