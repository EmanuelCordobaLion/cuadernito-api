package com.cuadernito.cuadernito_back.mapper;

import com.cuadernito.cuadernito_back.dto.TransactionDTO;
import com.cuadernito.cuadernito_back.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "type", expression = "java(transaction.getType().name())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "customerDebtId", source = "customerDebt.id")
    TransactionDTO toDTO(Transaction transaction);
}
