package com.example.lcx.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.ForeignKey;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SQLMapping;
import vn.com.lcx.common.annotation.TableName;

import java.math.BigInteger;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@SQLMapping
@TableName(value = "task", schema = "r_lcx")
public class TaskEntity {

    @IdColumn
    @ColumnName(name = "id")
    private BigInteger id;

    @ColumnName(name = "task_name", nullable = false)
    private String taskName;

    @ColumnName(name = "task_detail", defaultValue = "''")
    private String taskDetail;

    @ColumnName(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @ColumnName(name = "finished", defaultValue = "false")
    private Boolean finished;

    @ColumnName(name = "user_id")
    @ForeignKey(referenceColumn = "id", referenceTable = "user", cascade = true)
    private BigInteger userId;

    @ColumnName(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnName(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ColumnName(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ColumnName(name = "created_by")
    private String createdBy;

    @ColumnName(name = "updated_by")
    private String updatedBy;

}
