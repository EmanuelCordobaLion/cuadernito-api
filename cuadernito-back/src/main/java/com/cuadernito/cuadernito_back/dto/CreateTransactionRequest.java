package com.cuadernito.cuadernito_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request para crear una transacción. Solo incluye los campos que el usuario debe llenar.
 * id, userId, createdAt se asignan en el servidor y se devuelven en el response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear una transacción")
public class CreateTransactionRequest {

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Schema(description = "Monto de la transacción", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(description = "Descripción opcional")
    private String description;

    @Schema(description = "Tipo: INGRESO (por defecto) o GASTO")
    private String type;

    @Schema(description = "Fecha de la transacción (por defecto: ahora)")
    private LocalDateTime date;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "ID de la categoría", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "true si es fiado (venta a crédito)")
    private Boolean esFiado;

    @Schema(description = "ID de deuda existente (si es fiado y el cliente ya tiene deuda)")
    private Long customerDebtId;

    @Schema(description = "Monto fiado; si no se envía o es 0, se usa el monto completo")
    private BigDecimal debtAmount;

    @Schema(description = "Nombre del cliente (obligatorio si es fiado y cliente nuevo)")
    private String customerFirstName;

    @Schema(description = "Apellido del cliente (obligatorio si es fiado y cliente nuevo)")
    private String customerLastName;

    @Schema(description = "Teléfono del cliente (obligatorio si es fiado y cliente nuevo)")
    private String customerPhone;

    @Schema(description = "Número de documento del cliente (obligatorio si es fiado y cliente nuevo)")
    private String customerDocumentNumber;
}
