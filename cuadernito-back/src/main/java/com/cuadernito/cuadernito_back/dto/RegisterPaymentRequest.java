package com.cuadernito.cuadernito_back.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterPaymentRequest {
    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto del pago debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El monto tiene formato inválido")
    private BigDecimal amount;
}
