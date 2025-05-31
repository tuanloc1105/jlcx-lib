package vn.com.lcx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseUnixEntityDTO {
    private Long id;
    private BigInteger createdAt;
    private BigInteger updatedAt;
    private BigInteger deletedAt;
    private String createdBy;
    private String updatedBy;
}
