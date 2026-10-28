package com.example.usermanagement.service;

public interface JwtService {

    /** Genera un JWT firmado (HS256) para el correo indicado. */
    String generateToken(String email);

    /** Valida la firma y expiración, y devuelve el subject (correo). Lanza JwtException si es inválido. */
    String extractSubject(String token);
}
