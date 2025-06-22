package vn.com.lcx.jpa.dto;

import java.math.BigInteger;
import java.util.Objects;

public class BaseUnixEntityDTO {
    private Long id;
    private BigInteger createdAt;
    private BigInteger updatedAt;
    private BigInteger deletedAt;
    private String createdBy;
    private String updatedBy;

    public BaseUnixEntityDTO() {
    }

    public BaseUnixEntityDTO(Long id, BigInteger createdAt, BigInteger updatedAt, BigInteger deletedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigInteger getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(BigInteger createdAt) {
        this.createdAt = createdAt;
    }

    public BigInteger getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(BigInteger updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigInteger getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(BigInteger deletedAt) {
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
        BaseUnixEntityDTO that = (BaseUnixEntityDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt()) &&
                Objects.equals(getDeletedAt(), that.getDeletedAt()) &&
                Objects.equals(getCreatedBy(), that.getCreatedBy()) &&
                Objects.equals(getUpdatedBy(), that.getUpdatedBy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), getUpdatedAt(), getDeletedAt(), getCreatedBy(), getUpdatedBy());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseUnixEntityDTO{");
        sb.append("id=").append(id);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", deletedAt=").append(deletedAt);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
