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
@TableName(value = "user", schema = "lcx")
public class User implements Serializable {
    private static final long serialVersionUID = 2675938794277420417L;

    @IdColumn
    @ColumnName(name = "id")
    private Long id;

    @ColumnName(name = "username", nullable = false)
    private String username;

    @ColumnName(name = "password", nullable = false)
    private String password;

    @ColumnName(name = "full_name")
    private String fullName;

    @ColumnName(name = "active", defaultValue = "false")
    private Boolean active;

    @ColumnName(name = "created_time", defaultValue = "current_timestamp")
    private LocalDateTime createdTime;

    @ColumnName(name = "updated_time", defaultValue = "current_timestamp")
    private LocalDateTime updatedTime;

}
