package com.cuadernito.cuadernito_back.service;

import com.cuadernito.cuadernito_back.dto.UserDTO;
import com.cuadernito.cuadernito_back.dto.auth.*;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    UserDTO register(RegisterRequest registerRequest);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(String email, ChangePasswordRequest request);
}
