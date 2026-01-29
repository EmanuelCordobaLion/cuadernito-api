package com.cuadernito.cuadernito_back.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal amount;
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;
    private String type;
    private LocalDateTime date;
    private Long categoryId;
    private Long userId;
    private Long customerDebtId;
    private LocalDateTime createdAt;
}
