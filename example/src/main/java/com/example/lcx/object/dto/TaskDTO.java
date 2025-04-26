package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDTO {
    private Long id;
    private String taskName;
    private String taskDetail;
    private LocalDateTime remindAt;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Boolean finished;
    private String createdBy;
    private String updatedBy;
}
