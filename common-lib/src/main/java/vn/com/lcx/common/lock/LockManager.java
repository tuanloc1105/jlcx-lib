package vn.com.lcx.common.lock;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * LockManager - A utility class that provides file-based locking mechanism for process synchronization.
 * <p>
 * This class uses Java NIO's FileChannel and FileLock to implement a system-wide locking mechanism
 * that can be used to ensure exclusive access to resources across multiple JVM processes.
 * The lock is represented by a physical file on the filesystem.
 * <p>
 * Usage example:
 * <pre>
 * try (LockManager lockManager = new LockManager("/tmp/locks", "myProcess")) {
 *     lockManager.lock();
 *     // Execute critical section code here
 * } // Lock is automatically released when exiting the try-with-resources block
 * </pre>
 *
 * @since 1.0
 */
public class LockManager implements AutoCloseable {

    private final File file;
    private FileChannel channel = null;
    private FileLock lock = null;

    /**
     * Creates a new LockManager instance.
     *
     * @param lockFilePath The directory path where the lock file will be created
     * @param lockName     The name of the lock file (with or without .lock extension)
     * @throws IllegalArgumentException if lockFilePath or lockName is blank
     */
    public LockManager(String lockFilePath, String lockName) {
        if (StringUtils.isBlank(lockFilePath) || StringUtils.isBlank(lockName)) {
            throw new IllegalArgumentException("lockFilePath and lockName can't be blank");
        }
        FileUtils.createFolderIfNotExists(lockFilePath);
        if (lockName.contains(".lock")) {
            this.file = new File(FileUtils.pathJoining(lockFilePath, lockName));
        } else {
            this.file = new File(FileUtils.pathJoining(lockFilePath, lockName + ".lock"));
        }
    }

    /**
     * Acquires an exclusive lock on the file.
     * This method is synchronized to prevent concurrent lock attempts from the same instance.
     *
     * @throws LockException if the process is already locked, if the file cannot be created,
     *                       if the lock cannot be acquired, or if an I/O error occurs
     */
    public synchronized void lock() {
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                "creating file for locking:\n    - file name: {}",
                this.file.getAbsolutePath()
        );
        final var fileIsNotExist = !file.exists();
        if (fileIsNotExist) {
            try {
                LogUtils.writeLog(
                        LogUtils.Level.INFO,
                        this.file.createNewFile() ?
                                "named file does not exist and was successfully created" :
                                "named file already exists"
                );
            } catch (IOException e) {
                throw new LockException("Failed to create lock file", e);
            }
        }
        try {
            if (this.lock != null) {
                throw new LockException("Process already locked");
            }
            if (this.channel == null) {
                this.channel = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.WRITE);
            }
            this.lock = this.channel.tryLock();
            if (this.lock != null) {
                LogUtils.writeLog(LogUtils.Level.INFO, "File locked successfully");
            } else {
                throw new LockException("Failed to lock file, it may be locked by another process");
            }
        } catch (IOException e) {
            closeResources();
            throw new LockException("I/O error occurred while trying to lock file", e);
        }
    }

    /**
     * Attempts to acquire a lock with a specified timeout.
     *
     * @param timeout the maximum time to wait for the lock
     * @param unit    the time unit of the timeout argument
     * @return true if the lock was acquired, false if the timeout elapsed before the lock could be acquired
     * @throws LockException if an I/O error occurs during lock acquisition
     */
    public synchronized boolean tryLock(long timeout, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);

        while (System.currentTimeMillis() < endTime) {
            try {
                lock();
                return true;
            } catch (LockException e) {
                if (e.getMessage().contains("already locked")) {
                    throw e; // Re-throw if this instance already holds a lock
                }

                // Wait a bit before retrying
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the file is currently locked by this instance.
     *
     * @return true if the file is locked and the lock is valid, false otherwise
     */
    public boolean isLocked() {
        if (this.lock == null) {
            LogUtils.writeLog(LogUtils.Level.INFO, "Process is not being locked");
            return false;
        }
        final var lockIsValid = this.lock.isValid();
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                lockIsValid ?
                        "Process is being locked" : "Process is not being locked"
        );
        return lockIsValid;
    }

    /**
     * Convenience method that returns the negation of isLocked().
     *
     * @return true if the file is not locked, false if it is locked
     */
    public boolean isNotLocked() {
        return !this.isLocked();
    }

    /**
     * Releases the lock if it is held by this instance.
     * This method is synchronized to prevent concurrent release attempts.
     * After releasing the lock, it attempts to delete the lock file.
     *
     * @throws LockException if an I/O error occurs during lock release
     */
    public synchronized void releaseLock() {
        if (this.lock == null) {
            LogUtils.writeLog(LogUtils.Level.INFO, "Process is not being locked");
            return;
        }
        try {
            this.lock.release();
            closeResources();
            LogUtils.writeLog(LogUtils.Level.INFO, "Lock released successfully");
            final boolean deletedSuccessfully = this.file.delete();
            if (deletedSuccessfully) {
                LogUtils.writeLog(LogUtils.Level.INFO, "Lock file deleted successfully");
            } else {
                LogUtils.writeLog(LogUtils.Level.INFO, "Cannot delete lock file");
            }
        } catch (IOException e) {
            throw new LockException("Error releasing lock", e);
        }
    }

    /**
     * Closes resources (FileChannel and FileLock) without releasing the lock.
     * This is a helper method to ensure resources are properly cleaned up.
     */
    private void closeResources() {
        try {
            if (this.lock != null) {
                this.lock.close();
                this.lock = null;
            }
            if (this.channel != null) {
                this.channel.close();
                this.channel = null;
            }
        } catch (IOException e) {
            LogUtils.writeLog(LogUtils.Level.ERROR, "Error closing resources: {}", e.getMessage());
        }
    }

    /**
     * Returns the path of the lock file.
     *
     * @return the absolute path of the lock file
     */
    public String getLockFilePath() {
        return this.file.getAbsolutePath();
    }

    /**
     * Implements AutoCloseable interface to support try-with-resources.
     * Calls releaseLock() to ensure the lock is released when the resource is closed.
     */
    @Override
    public void close() {
        releaseLock();
    }

    /**
     * Exception class specific to locking operations.
     * This provides more specific error information than generic RuntimeExceptions.
     */
    public static class LockException extends RuntimeException {
        private static final long serialVersionUID = -3224950989580733347L;

        /**
         * Creates a new LockException with the specified message.
         *
         * @param message the detail message
         */
        public LockException(String message) {
            super(message);
        }

        /**
         * Creates a new LockException with the specified message and cause.
         *
         * @param message the detail message
         * @param cause   the cause of the exception
         */
        public LockException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
