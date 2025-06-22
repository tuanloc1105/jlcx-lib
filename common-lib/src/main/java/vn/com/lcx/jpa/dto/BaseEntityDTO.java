package vn.com.lcx.jpa.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class BaseEntityDTO {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String createdBy;
    private String updatedBy;

    public BaseEntityDTO() {
    }

    public BaseEntityDTO(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, String createdBy, String updatedBy) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntityDTO that = (BaseEntityDTO) o;
        return Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt()) &&
                Objects.equals(getDeletedAt(), that.getDeletedAt()) &&
                Objects.equals(getCreatedBy(), that.getCreatedBy()) &&
                Objects.equals(getUpdatedBy(), that.getUpdatedBy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCreatedAt(), getUpdatedAt(), getDeletedAt(), getCreatedBy(), getUpdatedBy());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseEntityDTO{");
        sb.append("createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", deletedAt=").append(deletedAt);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
