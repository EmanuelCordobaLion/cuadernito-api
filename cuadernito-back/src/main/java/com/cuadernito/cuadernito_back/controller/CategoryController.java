package com.cuadernito.cuadernito_back.controller;

import com.cuadernito.cuadernito_back.dto.CategoryDTO;
import com.cuadernito.cuadernito_back.dto.CreateCategoryRequest;
import com.cuadernito.cuadernito_back.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorías", description = "Endpoints para gestionar categorías de transacciones")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría. Solo envíe el nombre; id y userId se devuelven en el response.")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CategoryDTO dto = CategoryDTO.builder().name(request.getName()).build();
        CategoryDTO created = categoryService.createCategory(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica del usuario autenticado")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CategoryDTO category = categoryService.getCategoryById(id, email);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías del usuario autenticado")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        List<CategoryDTO> categories = categoryService.getAllCategoriesByUser(email);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente del usuario autenticado")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            @Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        CategoryDTO updated = categoryService.updateCategory(id, categoryDTO, email);
        return ResponseEntity.ok(updated);
    }
}
