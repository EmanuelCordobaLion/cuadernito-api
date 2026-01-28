package com.cuadernito.cuadernito_back.config;

import com.cuadernito.cuadernito_back.entity.User;
import com.cuadernito.cuadernito_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@cuadernito.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Cuadernito")
                    .email("admin@cuadernito.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .phone("1234567890")
                    .address("Dirección Admin")
                    .role(User.Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
