package com.example.lcx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SQLMapping;
import vn.com.lcx.common.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SQLMapping
@TableName(value = "task", schema = "lcx")
public class Task implements Serializable {
    private static final long serialVersionUID = -3944037378052993222L;

    @IdColumn
    @ColumnName(name = "id")
    private Long id;

    @ColumnName(name = "task_name", nullable = false)
    private String taskName;

    @ColumnName(name = "task_detail", defaultValue = "''")
    private String taskDetail;

    @ColumnName(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @ColumnName(name = "created_time", defaultValue = "current_timestamp")
    private LocalDateTime createdTime;

    @ColumnName(name = "updated_time", defaultValue = "current_timestamp")
    private LocalDateTime updatedTime;

    @ColumnName(name = "finished", defaultValue = "false")
    private Boolean finished;

    @ColumnName(name = "created_by", nullable = false)
    private String createdBy;

    @ColumnName(name = "updated_by", defaultValue = "''")
    private String updatedBy;

}
