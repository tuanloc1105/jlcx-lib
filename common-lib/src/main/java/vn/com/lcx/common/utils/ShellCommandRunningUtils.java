package vn.com.lcx.common.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ShellCommandRunningUtils {

    private ShellCommandRunningUtils() {
    }

    public static List<String> runWithProcessBuilder(String cmd, String directory, int... timeoutSecond) {
        try {
            if (StringUtils.isBlank(directory)) {
                directory = CommonConstant.ROOT_DIRECTORY_PROJECT_PATH;
            }
            LogUtils.writeLog(
                    LogUtils.Level.INFO,
                    "executing a command:\n    - command: {}\n    - directory: {}",
                    cmd,
                    directory
            );
            boolean isWindows = System
                    .getProperty("os.name")
                    .toLowerCase()
                    .startsWith("windows");
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                builder.command("cmd.exe", "/c", cmd);
            } else {
                builder.command("bash", "-c", cmd);
                // builder.command("sh", "-c", cmd);
            }
            builder.directory(new File(directory));
            Process process = builder.start();
            StreamGobbler stdOutStreamGobbler = new StreamGobbler(process.getInputStream(), String::trim);
            StreamGobbler errOutStreamGobbler = new StreamGobbler(process.getErrorStream(), String::trim);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<?> stdOutFuture = executorService.submit(stdOutStreamGobbler);
            Future<?> errOutFuture = executorService.submit(errOutStreamGobbler);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LogUtils.writeLog(
                        LogUtils.Level.WARN,
                        "\n" + String.join("\n", errOutStreamGobbler.getResult())
                );
                return new ArrayList<>();
            }
            stdOutFuture.get(timeoutSecond.length == 1 ? timeoutSecond[0] : 10, TimeUnit.SECONDS);
            errOutFuture.get(timeoutSecond.length == 1 ? timeoutSecond[0] : 10, TimeUnit.SECONDS);
            process.destroy();
            executorService.shutdown();
            LogUtils.writeLog(
                    LogUtils.Level.INFO,
                    "\n" + String.join("\n", stdOutStreamGobbler.getResult())
            );
            return stdOutStreamGobbler.getResult();
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Setter
    @Getter
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Function<String, String> consumer;
        private List<String> result;

        public StreamGobbler() {
            this.result = new ArrayList<>();
        }

        public StreamGobbler(InputStream inputStream, Function<String, String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.result = new ArrayList<>();
        }

        @Override
        public void run() {
            this.setResult(new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .map(consumer).collect(Collectors.toList()));
        }
    }
}
