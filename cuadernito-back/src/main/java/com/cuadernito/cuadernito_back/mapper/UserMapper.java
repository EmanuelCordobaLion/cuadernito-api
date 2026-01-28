package com.cuadernito.cuadernito_back.mapper;

import com.cuadernito.cuadernito_back.dto.UserDTO;
import com.cuadernito.cuadernito_back.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDTO toDTO(User user);
}
