package com.example.usermanagement.service.impl;

import com.example.usermanagement.dto.PhoneDTO;
import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.exception.BusinessException;
import com.example.usermanagement.model.Phone;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.JwtService;
import com.example.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           @Value("${app.validation.email-regex}") String emailRegex,
                           @Value("${app.security.password-regex}") String passwordRegex) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailPattern = Pattern.compile(emailRegex);
        this.passwordPattern = Pattern.compile(passwordRegex);
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO request) {
        if (request == null || isBlank(request.getName()) || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {
            throw new BusinessException("Los campos name, email y password son obligatorios", HttpStatus.BAD_REQUEST);
        }
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            throw new BusinessException("El formato del correo no es válido (debe ser aaaaaaa@dominio.cl)",
                    HttpStatus.BAD_REQUEST);
        }
        if (!passwordPattern.matcher(request.getPassword()).matches()) {
            throw new BusinessException("La contraseña no cumple con el formato requerido", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("El correo ya registrado", HttpStatus.CONFLICT);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhones(toPhones(request.getPhones()));
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setToken(jwtService.generateToken(request.getEmail()));
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    private List<Phone> toPhones(List<PhoneDTO> dtos) {
        List<Phone> phones = new ArrayList<>();
        if (dtos == null) {
            return phones;
        }
        for (PhoneDTO dto : dtos) {
            Phone phone = new Phone();
            phone.setNumber(dto.getNumber());
            phone.setCitycode(dto.getCitycode());
            phone.setContrycode(dto.getContrycode());
            phones.add(phone);
        }
        return phones;
    }

    private UserResponseDTO toResponse(User user) {
        List<PhoneDTO> phones = user.getPhones().stream()
                .map(p -> new PhoneDTO(p.getNumber(), p.getCitycode(), p.getContrycode()))
                .toList();
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phones(phones)
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isactive(user.getActive())
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
