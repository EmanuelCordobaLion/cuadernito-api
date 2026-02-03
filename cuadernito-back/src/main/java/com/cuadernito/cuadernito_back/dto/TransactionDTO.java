package com.cuadernito.cuadernito_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Transacción (response). Incluye datos del cliente cuando es fiado.")
public class TransactionDTO {
    @Schema(description = "ID (solo response)")
    private Long id;
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal amount;
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String description;
    private String type;
    private LocalDateTime date;
    private Long categoryId;
    @Schema(description = "ID del usuario (solo response)")
    private Long userId;
    @Schema(description = "ID de la deuda vinculada (solo response si es fiado)")
    private Long customerDebtId;
    @Schema(description = "Monto fiado de esta transacción. Null si no es fiado (solo response)")
    private BigDecimal debtAmount;
    @Schema(description = "true si está vinculada a una deuda (solo response)")
    private Boolean esFiado;
    @Schema(description = "Nombre del cliente (solo response cuando es fiado)")
    private String customerFirstName;
    @Schema(description = "Apellido del cliente (solo response cuando es fiado)")
    private String customerLastName;
    @Schema(description = "Teléfono del cliente (solo response cuando es fiado)")
    private String customerPhone;
    @Schema(description = "Documento del cliente (solo response cuando es fiado)")
    private String customerDocumentNumber;
    @Schema(description = "Fecha de creación (solo response)")
    private LocalDateTime createdAt;
}
