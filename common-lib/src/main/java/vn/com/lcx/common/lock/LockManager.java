package vn.com.lcx.common.lock;

import lombok.val;
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

public class LockManager {

    private final File file;
    private FileChannel channel = null;
    private FileLock lock = null;

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

    public synchronized void lock() {
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                "creating file for locking:\n    - file name: {}",
                this.file.getAbsolutePath()
        );
        val fileIsNotExist = !file.exists();
        if (fileIsNotExist) {
            try {
                LogUtils.writeLog(
                        LogUtils.Level.INFO,
                        this.file.createNewFile() ?
                                "named file does not exist and was successfully created" :
                                "named file already exists"
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            if (this.lock != null) {
                throw new RuntimeException("Process already locked");
            }
            if (this.channel == null) {
                this.channel = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.WRITE);
            }
            this.lock = this.channel.tryLock();
            if (this.lock != null) {
                LogUtils.writeLog(LogUtils.Level.INFO, "File locked successfully");
            } else {
                throw new RuntimeException("Failed to lock file");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // public boolean isLocked() {
    //     if (this.lock == null) {
    //         LogUtils.writeLog(LogUtils.Level.INFO, "Process is not being locked");
    //         return false;
    //     }
    //     val lockIsValid = this.lock.isValid();
    //     LogUtils.writeLog(
    //             LogUtils.Level.INFO,
    //             lockIsValid ?
    //                     "Process is being locked" : "Process is not being locked"
    //     );
    //     return lockIsValid;
    // }

    // public boolean isNotLocked() {
    //     return !this.isLocked();
    // }

    public synchronized void releaseLock() {
        if (this.lock == null) {
            LogUtils.writeLog(LogUtils.Level.INFO, "Process is not being locked");
            return;
        }
        try {
            this.lock.release();
            this.lock.close();
            this.channel.close();
            this.lock = null;
            this.channel = null;
            LogUtils.writeLog(LogUtils.Level.INFO, "Lock released successfully");
            final boolean deletedSuccessfully = this.file.delete();
            if (deletedSuccessfully) {
                LogUtils.writeLog(LogUtils.Level.INFO, "Lock file deleted successfully");
            } else {
                LogUtils.writeLog(LogUtils.Level.INFO, "Cannot delete lock file");
            }
            CommonUtils.gc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
