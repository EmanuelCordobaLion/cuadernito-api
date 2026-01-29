package com.cuadernito.cuadernito_back.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
public class CustomerDebtDTO {
    private Long id;
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String customerFirstName;
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    private String customerLastName;
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String customerPhone;
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El monto total tiene formato inválido")
    private BigDecimal totalAmount;
    @DecimalMin(value = "0", message = "El monto pagado no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El monto pagado tiene formato inválido")
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String status;
    private Long userId;
    private LocalDateTime createdAt;
}
