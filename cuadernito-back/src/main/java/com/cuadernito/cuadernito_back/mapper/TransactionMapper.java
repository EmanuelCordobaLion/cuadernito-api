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
    @Mapping(target = "debtAmount", source = "debtAmount")
    @Mapping(target = "esFiado", expression = "java(transaction.getCustomerDebt() != null)")
    @Mapping(target = "customerFirstName", expression = "java(transaction.getCustomerDebt() != null ? transaction.getCustomerDebt().getCustomerFirstName() : null)")
    @Mapping(target = "customerLastName", expression = "java(transaction.getCustomerDebt() != null ? transaction.getCustomerDebt().getCustomerLastName() : null)")
    @Mapping(target = "customerPhone", expression = "java(transaction.getCustomerDebt() != null ? transaction.getCustomerDebt().getCustomerPhone() : null)")
    @Mapping(target = "customerDocumentNumber", expression = "java(transaction.getCustomerDebt() != null ? transaction.getCustomerDebt().getDocumentNumber() : null)")
    TransactionDTO toDTO(Transaction transaction);
}
