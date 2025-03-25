package vn.com.lcx.common.utils;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ThreadUtils {

    private ThreadUtils() {
    }

    public static void logAllThreadsRunning() {
        Map<Thread, StackTraceElement[]> allThreads = getMapOfThreadAndStackTraceElement();
        for (Thread thread : allThreads.keySet()) {
            LogUtils.writeLog(LogUtils.Level.INFO, "Thread name: {} | State: {}", thread.getName(), thread.getState());
        }
    }

    public static boolean killThreadByName(final String threadName) {
        Map<Thread, StackTraceElement[]> allThreads = getMapOfThreadAndStackTraceElement();
        for (Thread thread : allThreads.keySet()) {
            if (threadName.equals(thread.getName())) {
                thread.interrupt();
                LogUtils.writeLog(LogUtils.Level.INFO, "Thread name: {} | State: {} has been interrupted", thread.getName(), thread.getState());
                return true;
            }
        }
        return false;
    }

    public static List<String> getAllRunningThreadsName() {
        val listOfThreadName = new ArrayList<String>();
        Map<Thread, StackTraceElement[]> allThreads = getMapOfThreadAndStackTraceElement();
        for (Thread thread : allThreads.keySet()) {
            listOfThreadName.add(thread.getName());
        }
        return listOfThreadName;
    }

    public static List<String> getAllRunningThreadsNameAndThreadState() {
        Map<Thread, StackTraceElement[]> allThreads = getMapOfThreadAndStackTraceElement();
        int longestThreadNameText = 0;
        int longestThreadStateText = 0;
        for (Thread thread : allThreads.keySet()) {
            if (thread.getName().length() > longestThreadNameText) {
                longestThreadNameText = thread.getName().length();
            }
            if (thread.getState().name().length() > longestThreadStateText) {
                longestThreadStateText = thread.getState().name().length();
            }
        }
        val threadInfoListToFormat = new ArrayList<List<String>>();
        final List<String> dsa = new ArrayList<>();
        for (Thread thread : allThreads.keySet()) {
            final List<String> threadInfoList = new ArrayList<>();
            threadInfoList.add(
                    String.format("Thread name: %s", thread.getName())
            );
            threadInfoList.add(
                    String.format("Thread state: %s", thread.getState())
            );
            threadInfoList.add(
                    String.format("Thread priority: %d", thread.getPriority())
            );

            threadInfoListToFormat.add(threadInfoList);

            final StackTraceElement[] stackTraceElements = thread.getStackTrace();

            final List<List<String>> asd = new ArrayList<>();

            if (stackTraceElements.length > 0) {
                for (int i = 0; i < stackTraceElements.length; i++) {
                    final ArrayList<String> stackTraceList = new ArrayList<>();
                    stackTraceList.add(String.format("Stack trace %d ", i));
                    stackTraceList.add(String.format("Class name: %s", stackTraceElements[i].getClassName()));
                    stackTraceList.add(String.format("Method name: %s", stackTraceElements[i].getMethodName()));
                    stackTraceList.add(String.format("File name: %s", stackTraceElements[i].getFileName()));
                    stackTraceList.add(String.format("Line number: %d", stackTraceElements[i].getLineNumber()));
                    asd.add(stackTraceList);
                }
                dsa.add(
                        "\n        - " + MyStringUtils.formatStringSpace2(
                                asd, "\n        - "
                        )
                );
            } else {
                dsa.add("No stack trace");
            }

        }
        final List<String> format = MyStringUtils.formatStringWithEqualSpaceLength(threadInfoListToFormat);
        for (int i = 0; i < format.size(); i++) {
            format.set(i, format.get(i) + dsa.get(i));
        }
        return format;
    }

    private static Map<Thread, StackTraceElement[]> getMapOfThreadAndStackTraceElement() {
        return Thread.getAllStackTraces();
    }

}
