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
public class TransactionDTO {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String type;
    private LocalDateTime date;
    private Long categoryId;
    private Long userId;
    private Long customerDebtId;
    private LocalDateTime createdAt;
}
