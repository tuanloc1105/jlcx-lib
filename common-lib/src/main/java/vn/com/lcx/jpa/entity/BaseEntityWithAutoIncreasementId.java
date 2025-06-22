package vn.com.lcx.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntityWithAutoIncreasementId extends BaseEntity {
    private static final long serialVersionUID = -8741039493441230522L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    public BaseEntityWithAutoIncreasementId(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, String createdBy, String updatedBy, Long id) {
        super(createdAt, updatedAt, deletedAt, createdBy, updatedBy);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseEntityWithAutoIncreasementId that = (BaseEntityWithAutoIncreasementId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
