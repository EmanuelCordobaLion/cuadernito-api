package com.cuadernito.cuadernito_back.mapper;

import com.cuadernito.cuadernito_back.dto.TransactionItemDTO;
import com.cuadernito.cuadernito_back.entity.TransactionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionItemMapper {
    @Mapping(target = "categoryId", source = "category.id")
    TransactionItemDTO toDTO(TransactionItem item);
    List<TransactionItemDTO> toDTOList(List<TransactionItem> items);
}
