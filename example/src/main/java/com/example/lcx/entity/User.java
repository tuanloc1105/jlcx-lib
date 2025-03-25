package com.example.lcx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SQLMapping;
import vn.com.lcx.common.annotation.TableName;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SQLMapping
@TableName(value = "user", schema = "public")
public class User {

    @IdColumn
    @ColumnName(name = "id")
    private Long id;

    @ColumnName(name = "first_name", nullable = false, defaultValue = "''", index = true)
    private String firstName;

    @ColumnName(name = "last_name", unique = true)
    private String lastName;

    @ColumnName(name = "age")
    private Integer age;

}
