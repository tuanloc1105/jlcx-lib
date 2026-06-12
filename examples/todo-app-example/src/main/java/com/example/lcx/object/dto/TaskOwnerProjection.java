package com.example.lcx.object.dto;

import vn.io.lcx.common.annotation.ColumnName;
import vn.io.lcx.common.annotation.SQLProjection;

import java.math.BigInteger;

@SQLProjection
public class TaskOwnerProjection {

    @ColumnName(name = "TASK_ID")
    private BigInteger taskId;

    @ColumnName(name = "TASK_NAME")
    private String taskName;

    @ColumnName(name = "OWNER_USERNAME")
    private String ownerUsername;

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
