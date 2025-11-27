package vn.com.lcx.vertx.base.http.response;

import java.util.Objects;

/**
 * Simple holder representing a file that should be streamed via Vert.x.
 */
public class FileEntity {

    private final String filePath;
    private final boolean deleteAfterSend;

    private FileEntity(Builder builder) {
        this.filePath = Objects.requireNonNull(builder.filePath, "filePath is required");
        this.deleteAfterSend = builder.deleteAfterSend;
    }

    public FileEntity(String filePath) {
        this(filePath, false);
    }

    public FileEntity(String filePath, boolean deleteAfterSend) {
        this.filePath = Objects.requireNonNull(filePath, "filePath is required");
        this.deleteAfterSend = deleteAfterSend;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isDeleteAfterSend() {
        return deleteAfterSend;
    }

    public static final class Builder {
        private String filePath;
        private boolean deleteAfterSend;

        private Builder() {
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder deleteAfterSend(boolean deleteAfterSend) {
            this.deleteAfterSend = deleteAfterSend;
            return this;
        }

        public FileEntity build() {
            return new FileEntity(this);
        }
    }
}

