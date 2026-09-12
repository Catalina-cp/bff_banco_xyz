package com.bank.bffweb.controller;

import com.bank.bffweb.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de autenticacion para el canal Web.
 *
 * El cliente envia su API Key en el header X-API-KEY. Si es valida,
 * recibe a cambio un JWT que debe usar en el header Authorization
 * (formato "Bearer <token>") para acceder a los endpoints protegidos.
 */
@RestController
public class AuthController {

    @Value("${bff.api-key}")
    private String apiKeyValida;

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // POST https://localhost:8081/auth/token
    // header: X-API-KEY: web-secret-123
    @PostMapping("/auth/token")
    public ResponseEntity<?> obtenerToken(@RequestHeader("X-API-KEY") String apiKeyRecibida) {

        if (!apiKeyValida.equals(apiKeyRecibida)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("API Key invalida.");
        }

        String token = jwtUtil.generarToken("canal-web", "ROLE_WEB");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", "Bearer",
                "expiraEn", "1 hora"
        ));
    }
}