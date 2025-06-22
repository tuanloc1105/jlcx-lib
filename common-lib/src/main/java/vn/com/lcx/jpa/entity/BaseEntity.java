package vn.com.lcx.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import vn.com.lcx.common.utils.DateTimeUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 4706423758892459069L;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    {
        createdAt = Instant
                .ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.of(ZoneId.SHORT_IDS.get(DateTimeUtils.TimezoneEnum.VST.name())))
                .toLocalDateTime();
        updatedAt = Instant
                .ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.of(ZoneId.SHORT_IDS.get(DateTimeUtils.TimezoneEnum.VST.name())))
                .toLocalDateTime();
    }

    public BaseEntity() {
    }

    public BaseEntity(LocalDateTime createdAt,
                      LocalDateTime updatedAt,
                      LocalDateTime deletedAt,
                      String createdBy,
                      String updatedBy) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
        BaseEntity that = (BaseEntity) o;
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
}
