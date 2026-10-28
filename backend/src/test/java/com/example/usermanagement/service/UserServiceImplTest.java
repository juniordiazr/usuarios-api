package com.example.usermanagement.service;

import com.example.usermanagement.dto.PhoneDTO;
import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.exception.BusinessException;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.cl$";
    private static final String PASS_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,12}$";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, passwordEncoder, jwtService, EMAIL_REGEX, PASS_REGEX);
    }

    private UserRequestDTO validRequest() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Juan Rodriguez");
        dto.setEmail("juan@rodriguez.cl");
        dto.setPassword("Password1");
        dto.setPhones(List.of(new PhoneDTO("1234567", "1", "57")));
        return dto;
    }

    @Test
    void registraUsuarioValido() {
        when(userRepository.existsByEmailIgnoreCase("juan@rodriguez.cl")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hash");
        when(jwtService.generateToken("juan@rodriguez.cl")).thenReturn("jwt.token.demo");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponseDTO res = service.registerUser(validRequest());

        assertNotNull(res.getId());
        assertEquals("jwt.token.demo", res.getToken());
        assertTrue(res.getIsactive());
        assertEquals(res.getCreated(), res.getLastLogin());
        assertEquals(1, res.getPhones().size());
    }

    @Test
    void rechazaCorreoConFormatoInvalido() {
        UserRequestDTO dto = validRequest();
        dto.setEmail("juan@rodriguez.org");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerUser(dto));
        assertTrue(ex.getMessage().contains("correo"));
    }

    @Test
    void rechazaContrasenaInvalida() {
        UserRequestDTO dto = validRequest();
        dto.setPassword("hunter2");
        assertThrows(BusinessException.class, () -> service.registerUser(dto));
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(userRepository.existsByEmailIgnoreCase("juan@rodriguez.cl")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerUser(validRequest()));
        assertEquals("El correo ya registrado", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
