package com.cuadernito.cuadernito_back.service;

import com.cuadernito.cuadernito_back.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO, String userEmail);
    CategoryDTO getCategoryById(Long id, String userEmail);
    List<CategoryDTO> getAllCategoriesByUser(String userEmail);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String userEmail);
}
