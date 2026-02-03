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

/**
 * Request para actualizar una transacción (parcial). Solo envíe los campos que desea cambiar.
 * id, userId, createdAt no se envían; se devuelven en el response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para actualizar una transacción (parcial)")
public class UpdateTransactionRequest {

    @Positive(message = "El monto debe ser mayor que cero")
    @Schema(description = "Nuevo monto")
    private BigDecimal amount;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(description = "Nueva descripción")
    private String description;

    @Schema(description = "Nuevo tipo: INGRESO o GASTO")
    private String type;

    @Schema(description = "Nueva fecha")
    private LocalDateTime date;

    @Schema(description = "Nuevo ID de categoría")
    private Long categoryId;

    @Schema(description = "true/false para marcar o desmarcar como fiado")
    private Boolean esFiado;

    @Schema(description = "ID de deuda existente (si es fiado)")
    private Long customerDebtId;

    @Schema(description = "Nuevo monto fiado")
    private BigDecimal debtAmount;

    @Schema(description = "Nombre del cliente (si es fiado y cliente nuevo)")
    private String customerFirstName;

    @Schema(description = "Apellido del cliente (si es fiado y cliente nuevo)")
    private String customerLastName;

    @Schema(description = "Teléfono del cliente (si es fiado y cliente nuevo)")
    private String customerPhone;

    @Schema(description = "Número de documento (si es fiado y cliente nuevo)")
    private String customerDocumentNumber;
}
