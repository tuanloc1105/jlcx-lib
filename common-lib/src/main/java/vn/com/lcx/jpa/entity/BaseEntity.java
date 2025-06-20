package vn.com.lcx.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import vn.com.lcx.common.utils.DateTimeUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@MappedSuperclass
@Data
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

}
