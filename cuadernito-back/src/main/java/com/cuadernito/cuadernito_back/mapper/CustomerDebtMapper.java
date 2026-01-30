package com.cuadernito.cuadernito_back.mapper;

import com.cuadernito.cuadernito_back.dto.CustomerDebtDTO;
import com.cuadernito.cuadernito_back.entity.CustomerDebt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerDebtMapper {
    @Mapping(target = "status", expression = "java(customerDebt.getStatus().name())")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    CustomerDebtDTO toDTO(CustomerDebt customerDebt);
}
