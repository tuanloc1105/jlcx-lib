package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserJWTTokenInfo {
    private Long id;
    private String username;
    private String fullName;
}
