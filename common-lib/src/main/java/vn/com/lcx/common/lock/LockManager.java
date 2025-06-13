package vn.com.lcx.common.lock;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.utils.CommonUtils;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * A utility class that provides file-based locking mechanism to ensure thread-safe operations.
 * This class uses {@link FileLock} to create and manage locks at the file system level,
 * which can be used to prevent multiple instances of an application from running concurrently
 * or to synchronize access to shared resources across different JVMs.
 *
 * <p>This implementation is thread-safe and handles proper resource cleanup.
 *
 * <p>Usage example:
 * <pre>{@code
 * LockManager lockManager = new LockManager("/var/lock", "myapp.lock");
 * try {
 *     lockManager.lock();
 *     // Critical section
 * } finally {
 *     lockManager.releaseLock();
 * }
 * }</pre>
 *
 * @see FileLock
 * @see FileChannel
 */
public class LockManager {
    /** 
     * The lock file used for synchronization. 
     * Will be created in the specified directory with .lock extension if not provided.
     */
    private final File file;
    
    /** 
     * The file channel used for obtaining file locks.
     * Initialized when first needed and closed when lock is released.
     */
    private FileChannel channel = null;
    
    /** 
     * The current file lock, or null if not locked. 
     * Represents the actual OS-level lock on the file.
     */
    private FileLock lock = null;

    /**
     * Creates a new LockManager instance with the specified lock file path and name.
     * The lock file will be created in the specified directory if it doesn't exist.
     * If the provided lockName doesn't end with ".lock", it will be automatically appended.
     *
     * @param lockFilePath the directory path where the lock file will be created.
     *                    Must not be null or blank.
     * @param lockName    the name of the lock file. Can be with or without .lock extension.
     *                   Must not be null or blank.
     * @throws IllegalArgumentException if either lockFilePath or lockName is blank
     * @throws RuntimeException if there's an error creating the lock directory
     * @see FileUtils#createFolderIfNotExists(String)
     * @see FileUtils#pathJoining(String...)
     */
    public LockManager(String lockFilePath, String lockName) {
        if (StringUtils.isBlank(lockFilePath) || StringUtils.isBlank(lockName)) {
            throw new IllegalArgumentException("lockFilePath and lockName can't be blank");
        }
        FileUtils.createFolderIfNotExists(lockFilePath);
        if (lockName.endsWith(".lock")) {
            this.file = new File(FileUtils.pathJoining(lockFilePath, lockName));
        } else {
            this.file = new File(FileUtils.pathJoining(lockFilePath, lockName + ".lock"));
        }
    }

    /**
     * Acquires an exclusive lock on the lock file.
     * <p>
     * This method will:
     * <ol>
     *   <li>Create the lock file if it doesn't exist</li>
     *   <li>Open a file channel to the lock file</li>
     *   <li>Attempt to acquire an exclusive lock on the file</li>
     * </ol>
     * 
     * <p>This method is thread-safe and idempotent. If the lock is already held by this instance,
     * it will throw an IllegalStateException.
     *
     * @throws IllegalStateException if the lock is already held by this instance
     * @throws RuntimeException if the lock is held by another process or if an I/O error occurs
     * @throws SecurityException if a security manager exists and denies the operation
     * @see #releaseLock()
     * @see FileChannel#tryLock()
     */
    public synchronized void lock() {
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                "creating file for locking:\n    - file name: {}",
                this.file.getAbsolutePath()
        );
        
        // Create the lock file if it doesn't exist
        final var fileIsNotExist = !file.exists();
        if (fileIsNotExist) {
            try {
                boolean created = this.file.createNewFile();
                LogUtils.writeLog(
                        LogUtils.Level.INFO,
                        created ? "Created new lock file" : "Lock file already exists"
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to create lock file: " + e.getMessage(), e);
            }
        }
        
        try {
            // Check if already locked by this instance
            if (this.lock != null) {
                throw new IllegalStateException("Lock already acquired by this instance");
            }
            
            // Open file channel if not already open
            if (this.channel == null) {
                this.channel = FileChannel.open(
                    Paths.get(file.getAbsolutePath()),
                    StandardOpenOption.WRITE
                );
            }
            
            // Try to acquire the lock
            this.lock = this.channel.tryLock();
            if (this.lock != null) {
                LogUtils.writeLog(LogUtils.Level.INFO, "Successfully acquired file lock");
            } else {
                throw new RuntimeException("Cannot acquire lock - file may be locked by another process");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to acquire file lock: " + e.getMessage(), e);
        }
    }

    /**
     * Releases the currently held lock and cleans up all associated resources.
     * <p>
     * This method will:
     * <ol>
     *   <li>Release the file lock</li>
     *   <li>Close the file channel</li>
     *   <li>Attempt to delete the lock file</li>
     *   <li>Suggest garbage collection of native resources</li>
     * </ol>
     * 
     * <p>This method is idempotent - calling it multiple times is safe and has no effect
     * if no lock is currently held.
     *
     * @throws RuntimeException if an I/O error occurs while releasing resources
     * @see #lock()
     * @see FileLock#release()
     * @see FileChannel#close()
     */
    public synchronized void releaseLock() {
        // If no lock is held, nothing to do
        if (this.lock == null) {
            LogUtils.writeLog(LogUtils.Level.INFO, "No active lock to release");
            return;
        }
        
        try {
            // Release and close the lock
            this.lock.release();
            this.lock.close();
            
            // Close the channel
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }
            
            // Clear references
            this.lock = null;
            this.channel = null;
            
            LogUtils.writeLog(LogUtils.Level.INFO, "Successfully released file lock");
            
            // Try to clean up the lock file
            cleanupLockFile();
            
        } catch (IOException e) {
            throw new RuntimeException("Error while releasing lock: " + e.getMessage(), e);
        } finally {
            // Always suggest GC to clean up native resources
            CommonUtils.gc();
        }
    }
    
    /**
     * Attempts to delete the lock file.
     * Logs a warning if the file cannot be deleted.
     */
    private void cleanupLockFile() {
        if (this.file != null && this.file.exists()) {
            try {
                if (this.file.delete()) {
                    LogUtils.writeLog(LogUtils.Level.INFO, "Successfully deleted lock file");
                } else {
                    LogUtils.writeLog(LogUtils.Level.WARN, 
                        "Could not delete lock file. It may be locked by another process: " + 
                        this.file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                LogUtils.writeLog(LogUtils.Level.WARN, 
                    "Security exception while deleting lock file: " + e.getMessage());
            }
        }
    }
}
