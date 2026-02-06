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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para actualizar una transacción (parcial)")
public class UpdateTransactionRequest {

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(description = "Nueva descripción")
    private String description;

    @Schema(description = "Nuevo tipo: INGRESO o GASTO")
    private String type;

    @Schema(description = "Nueva fecha")
    private LocalDateTime date;

    @jakarta.validation.Valid
    @Schema(description = "Items actualizados. Items con id = actualizar, items sin id = crear nuevo. Si se envía, reemplaza los items existentes (excepto los que están en removeItemIds).")
    private List<TransactionItemDTO> items;

    @Schema(description = "IDs de items a eliminar")
    private List<Long> removeItemIds;

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
