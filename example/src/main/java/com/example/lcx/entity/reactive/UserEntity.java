package com.example.lcx.entity.reactive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.com.lcx.common.annotation.ColumnName;
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
@TableName(value = "user", schema = "r_lcx")
public class UserEntity {

    @IdColumn
    @ColumnName(name = "id")
    private BigInteger id;

    @ColumnName(name = "username", nullable = false)
    private String username;

    @ColumnName(name = "password", nullable = false)
    private String password;

    @ColumnName(name = "full_name")
    private String fullName;

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
