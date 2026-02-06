package com.cuadernito.cuadernito_back.service;

import com.cuadernito.cuadernito_back.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO deleteUser(Long id);
}
