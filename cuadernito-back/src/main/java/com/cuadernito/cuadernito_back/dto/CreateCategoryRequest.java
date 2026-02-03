package com.cuadernito.cuadernito_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para crear una categoría. Solo el nombre; id y userId se asignan en el servidor.")
public class CreateCategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(description = "Nombre de la categoría", example = "Ventas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
