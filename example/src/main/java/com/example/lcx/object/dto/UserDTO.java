package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private Boolean active;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
