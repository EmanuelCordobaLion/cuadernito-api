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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear una transacción")
public class CreateTransactionRequest {

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(description = "Descripción opcional")
    private String description;

    @Schema(description = "Tipo: INGRESO (por defecto) o GASTO")
    private String type;

    @Schema(description = "Fecha de la transacción (por defecto: ahora)")
    private LocalDateTime date;

    @NotNull(message = "Los items son obligatorios")
    @jakarta.validation.Valid
    @Schema(description = "Items de la transacción (desglose por categoría). Mínimo 1 item.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TransactionItemDTO> items;

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
