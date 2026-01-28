package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.CategoryDTO;
import com.cuadernito.cuadernito_back.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO, String userEmail) {
        return null;
    }

    @Override
    public CategoryDTO getCategoryById(Long id, String userEmail) {
        return null;
    }

    @Override
    public List<CategoryDTO> getAllCategoriesByUser(String userEmail) {
        return null;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String userEmail) {
        return null;
    }
}
