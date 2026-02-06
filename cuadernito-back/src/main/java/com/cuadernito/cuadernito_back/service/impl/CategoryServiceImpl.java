package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.CategoryDTO;
import com.cuadernito.cuadernito_back.entity.Category;
import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.exception.BadRequestException;
import com.cuadernito.cuadernito_back.exception.ResourceNotFoundException;
import com.cuadernito.cuadernito_back.mapper.CategoryMapper;
import com.cuadernito.cuadernito_back.repository.CategoryRepository;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import com.cuadernito.cuadernito_back.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Category> categories = categoryRepository.findByUserId(user.getId());
        return categories.stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la categoría no puede estar vacío");
        }

        category.setName(categoryDTO.getName().trim());
        
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDTO(updatedCategory);
    }
}
