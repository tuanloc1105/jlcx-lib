package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReactiveTaskDTO {
    private BigInteger id;
    private String taskName;
    private String taskDetail;
    private LocalDateTime remindAt;
    private Boolean finished;
    private BigInteger userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String createdBy;
    private String updatedBy;
}
