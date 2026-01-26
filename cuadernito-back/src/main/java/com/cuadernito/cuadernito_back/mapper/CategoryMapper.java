package com.cuadernito.cuadernito_back.mapper;

import com.cuadernito.cuadernito_back.dto.CategoryDTO;
import com.cuadernito.cuadernito_back.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "userId", source = "user.id")
    CategoryDTO toDTO(Category category);
}
