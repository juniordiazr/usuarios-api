package com.example.usermanagement.service;

import com.example.usermanagement.service.impl.JwtServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private static final String SECRET = "un-secreto-de-pruebas-con-mas-de-32-caracteres";

    @Test
    void generaTokenConTresPartesYRecuperaElCorreo() {
        JwtServiceImpl jwt = new JwtServiceImpl(SECRET, 60);
        String token = jwt.generateToken("juan@rodriguez.cl");

        assertEquals(3, token.split("\\.").length);
        assertEquals("juan@rodriguez.cl", jwt.extractSubject(token));
    }

    @Test
    void rechazaTokenFirmadoConOtroSecreto() {
        String token = new JwtServiceImpl(SECRET, 60).generateToken("juan@rodriguez.cl");
        JwtServiceImpl otro = new JwtServiceImpl("otro-secreto-distinto-con-mas-de-32-caracteres", 60);

        assertThrows(JwtException.class, () -> otro.extractSubject(token));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtServiceImpl jwt = new JwtServiceImpl(SECRET, -1);
        String token = jwt.generateToken("juan@rodriguez.cl");

        assertThrows(JwtException.class, () -> jwt.extractSubject(token));
    }
}
