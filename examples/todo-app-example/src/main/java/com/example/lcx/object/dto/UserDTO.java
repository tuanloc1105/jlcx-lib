package com.example.lcx.object.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.jpa.dto.BaseEntityDTO;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO extends BaseEntityDTO {
    private BigInteger id;
    private String username;
    private String fullName;
}
