package com.cuadernito.cuadernito_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Item de una transacción (desglose por categoría)")
public class TransactionItemDTO {
    @Schema(description = "ID del item (solo response o para actualizar)")
    private Long id;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "ID de la categoría", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Schema(description = "Monto de este item", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "Fecha de creación (solo response)")
    private LocalDateTime createdAt;
}
