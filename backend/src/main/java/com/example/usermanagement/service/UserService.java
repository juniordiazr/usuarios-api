package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO request);
}
