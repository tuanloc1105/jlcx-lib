package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.jpa.dto.BaseEntityDTO;

import java.math.BigInteger;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDTO extends BaseEntityDTO {
    private BigInteger id;
    private String taskName;
    private String taskDetail;
    private LocalDateTime remindAt;
    private Boolean finished;
}
