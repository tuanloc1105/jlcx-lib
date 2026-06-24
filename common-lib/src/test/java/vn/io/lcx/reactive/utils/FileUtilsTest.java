package vn.io.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    private Vertx vertx;

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (vertx != null) {
            await(vertx.close());
        }
    }

    @Test
    void appendStringToExistingFile_appendsContent() throws Exception {
        Path file = tempDir.resolve("existing.txt");
        Files.writeString(file, "first", StandardCharsets.UTF_8);

        await(FileUtils.appendToFile(vertx, file.toString(), " second"));

        assertEquals("first second", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void appendStringToMissingFile_createsFile() throws Exception {
        Path file = tempDir.resolve("created-string.txt");

        await(FileUtils.appendToFile(vertx, file.toString(), "created"));

        assertTrue(Files.exists(file));
        assertEquals("created", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void appendBufferToMissingFile_createsFile() throws Exception {
        Path file = tempDir.resolve("created-buffer.txt");

        await(FileUtils.appendToFile(vertx, file.toString(), Buffer.buffer("binary", "UTF-8")));

        assertTrue(Files.exists(file));
        assertEquals("binary", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void processFileLineByLine_trimsNonEmptyLinesAndProcessesFinalLine() throws Exception {
        Path file = tempDir.resolve("lines.txt");
        Files.writeString(file, " alpha \n\n beta \n   \ngamma", StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();

        await(FileUtils.processFileLineByLine(vertx, file.toString(), line -> {
            lines.add(line);
            return Future.succeededFuture();
        }));

        assertEquals(List.of("alpha", "beta", "gamma"), lines);
    }

    @Test
    void processFileLineByLine_waitsForAsyncHandlerCompletion() throws Exception {
        Path file = tempDir.resolve("async-lines.txt");
        Files.writeString(file, "delayed\n", StandardCharsets.UTF_8);
        Promise<Void> handlerCompletion = Promise.promise();
        CompletableFuture<String> handledLine = new CompletableFuture<>();

        Future<Void> result = FileUtils.processFileLineByLine(vertx, file.toString(), line -> {
            handledLine.complete(line);
            return handlerCompletion.future();
        });

        assertEquals("delayed", handledLine.get(5, TimeUnit.SECONDS));
        assertFalse(result.isComplete());

        handlerCompletion.complete();
        await(result);
        assertTrue(result.succeeded());
    }

    @Test
    void processFileLineByLine_failedHandlerFailsResult() throws Exception {
        Path file = tempDir.resolve("failed-handler.txt");
        Files.writeString(file, "line\n", StandardCharsets.UTF_8);
        RuntimeException failure = new RuntimeException("boom");

        Throwable result = awaitFailure(FileUtils.processFileLineByLine(vertx, file.toString(), line -> Future.failedFuture(failure)));

        assertSame(failure, result);
    }

    @Test
    void processFileLineByLine_thrownHandlerExceptionFailsResult() throws Exception {
        Path file = tempDir.resolve("thrown-handler.txt");
        Files.writeString(file, "line\n", StandardCharsets.UTF_8);
        RuntimeException failure = new RuntimeException("boom");

        Throwable result = awaitFailure(FileUtils.processFileLineByLine(vertx, file.toString(), line -> {
            throw failure;
        }));

        assertSame(failure, result);
    }

    @Test
    void processFileLineByLine_largeLineIsDeliveredAsOneLine() throws Exception {
        Path file = tempDir.resolve("large-line.txt");
        String largeLine = ("a".repeat(8191) + "€").repeat(16);
        Files.writeString(file, largeLine + "\nsecond", StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();

        await(FileUtils.processFileLineByLine(vertx, file.toString(), line -> {
            lines.add(line);
            return Future.succeededFuture();
        }));

        assertEquals(List.of(largeLine, "second"), lines);
    }

    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    private static Throwable awaitFailure(Future<?> future) {
        ExecutionException exception = assertThrows(ExecutionException.class,
                () -> future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
        return exception.getCause();
    }
}
