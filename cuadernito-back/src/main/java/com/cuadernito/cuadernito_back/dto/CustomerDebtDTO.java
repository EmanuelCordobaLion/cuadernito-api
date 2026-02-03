package com.cuadernito.cuadernito_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Deuda de un cliente (fiado). Estructura: datos del cliente, montos de la deuda, estado.")
public class CustomerDebtDTO {

    private Long id;

    @Schema(description = "Nombre del cliente")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String customerFirstName;

    @Schema(description = "Apellido del cliente")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    private String customerLastName;

    @Schema(description = "Teléfono del cliente")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String customerPhone;

    @Schema(description = "Número de documento del cliente")
    @Size(max = 50, message = "El número de documento no puede superar 50 caracteres")
    private String documentNumber;

    @Schema(description = "Monto total de la deuda (lo que debe el cliente en total)")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El monto total tiene formato inválido")
    private BigDecimal totalAmount;

    @Schema(description = "Monto ya pagado por el cliente")
    @DecimalMin(value = "0", message = "El monto pagado no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El monto pagado tiene formato inválido")
    private BigDecimal paidAmount;

    @Schema(description = "Monto pendiente por pagar (totalAmount - paidAmount)")
    private BigDecimal remainingAmount;

    @Schema(description = "Estado: PENDIENTE (no pagó nada), PARCIAL (pagó algo), PAGADO (deuda saldada)")
    private String status;

    private Long userId;
    private LocalDateTime createdAt;
}
