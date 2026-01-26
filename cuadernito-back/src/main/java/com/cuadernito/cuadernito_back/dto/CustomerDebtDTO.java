package com.cuadernito.cuadernito_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDebtDTO {
    private Long id;
    private String customerFirstName;
    private String customerLastName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String status;
    private Long userId;
    private LocalDateTime createdAt;
}
