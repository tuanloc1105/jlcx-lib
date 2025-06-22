package vn.com.lcx.common.utils;

import org.apache.commons.collections4.CollectionUtils;
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

    public static ShellCommandOutput runWithProcessBuilder(String cmd,
                                                           String directory,
                                                           int timeoutSecond,
                                                           List<ProcessEnvironment> environments,
                                                           boolean showLog) {
        Process process = null;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        int exitCode = -999;
        final var stdOut = new ArrayList<String>();
        final var stdErr = new ArrayList<String>();
        try {
            if (StringUtils.isBlank(directory)) {
                directory = CommonConstant.ROOT_DIRECTORY_PROJECT_PATH;
            }
            if (showLog) {
                LogUtils.writeLog(
                        LogUtils.Level.INFO,
                        "executing a command:\n    - command: {}\n    - directory: {}",
                        cmd,
                        directory
                );
            }
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
            if (CollectionUtils.isNotEmpty(environments)) {
                environments
                        .forEach(
                                environment ->
                                        builder.environment().put(environment.getEnvName(), environment.getEnvValue())
                        );
            }
            builder.directory(new File(directory));
            process = builder.start();
            StreamGobbler stdOutStreamGobbler = new StreamGobbler(process.getInputStream(), String::trim);
            StreamGobbler errOutStreamGobbler = new StreamGobbler(process.getErrorStream(), String::trim);
            Future<?> stdOutFuture = executorService.submit(stdOutStreamGobbler);
            Future<?> errOutFuture = executorService.submit(errOutStreamGobbler);
            boolean executedSuccess = process.waitFor(timeoutSecond > 0 ? timeoutSecond : 10, TimeUnit.SECONDS);
            if (executedSuccess) {
                exitCode = process.exitValue();
                stdOutFuture.get(timeoutSecond > 0 ? timeoutSecond : 10, TimeUnit.SECONDS);
                errOutFuture.get(timeoutSecond > 0 ? timeoutSecond : 10, TimeUnit.SECONDS);
                stdErr.addAll(errOutStreamGobbler.getResult());
                stdOut.addAll(stdOutStreamGobbler.getResult());
            }
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
            exitCode = -999;
            stdErr.add(ExceptionUtils.getStackTrace(e));
        } finally {
            if (process != null) {
                process.destroy();
            }
            executorService.shutdown();
        }
        return new ShellCommandOutput(exitCode, stdOut, stdErr);
    }

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

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public Function<String, String> getConsumer() {
            return consumer;
        }

        public void setConsumer(Function<String, String> consumer) {
            this.consumer = consumer;
        }

        public List<String> getResult() {
            return result;
        }

        public void setResult(List<String> result) {
            this.result = result;
        }

        @Override
        public void run() {
            this.setResult(new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .map(consumer).collect(Collectors.toList()));
        }
    }

    public static class ProcessEnvironment {
        private final String envName;
        private final String envValue;

        public ProcessEnvironment(String envName, String envValue) {
            this.envName = envName;
            this.envValue = envValue;
        }

        public String getEnvName() {
            return envName;
        }

        public String getEnvValue() {
            return envValue;
        }
    }

    public static class ShellCommandOutput {
        private final int exitCode;
        private final List<String> stdOut;
        private final List<String> stdErr;

        public ShellCommandOutput(int exitCode, List<String> stdOut, List<String> stdErr) {
            this.exitCode = exitCode;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public List<String> getStdOut() {
            return stdOut;
        }

        public List<String> getStdErr() {
            return stdErr;
        }

    }

}
