package com.cuadernito.cuadernito_back.service.impl;

import com.cuadernito.cuadernito_back.dto.UserDTO;
import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.exception.BadRequestException;
import com.cuadernito.cuadernito_back.exception.ResourceNotFoundException;
import com.cuadernito.cuadernito_back.mapper.UserMapper;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import com.cuadernito.cuadernito_back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (userDTO.getFirstName() != null && !userDTO.getFirstName().trim().isEmpty()) {
            user.setFirstName(userDTO.getFirstName().trim());
        }

        if (userDTO.getLastName() != null && !userDTO.getLastName().trim().isEmpty()) {
            user.setLastName(userDTO.getLastName().trim());
        }

        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            String newEmail = userDTO.getEmail().trim();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("El email ya está registrado");
            }
            user.setEmail(newEmail);
        }

        if (userDTO.getPhone() != null && !userDTO.getPhone().trim().isEmpty()) {
            user.setPhone(userDTO.getPhone().trim());
        }

        if (userDTO.getAddress() != null && !userDTO.getAddress().trim().isEmpty()) {
            user.setAddress(userDTO.getAddress().trim());
        }

        if (userDTO.getRole() != null && !userDTO.getRole().trim().isEmpty()) {
            try {
                User.Role role = User.Role.valueOf(userDTO.getRole());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rol inválido. Los roles válidos son: ROLE_ADMIN, ROLE_USER");
            }
        }

        if (userDTO.getEnabled() != null) {
            user.setEnabled(userDTO.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserDTO deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        
        UserDTO userDTO = userMapper.toDTO(user);
        userRepository.deleteById(id);
        
        return userDTO;
    }
}
