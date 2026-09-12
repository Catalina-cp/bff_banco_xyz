package com.bank.bffatm.controller;

import com.bank.bffatm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de autenticacion para el canal Cajero Automatico.
 */
@RestController
public class AuthController {

    @Value("${bff.api-key}")
    private String apiKeyValida;

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // POST https://localhost:8083/auth/token
    // header: X-API-KEY: atm-secret-789
    @PostMapping("/auth/token")
    public ResponseEntity<?> obtenerToken(@RequestHeader("X-API-KEY") String apiKeyRecibida) {

        if (!apiKeyValida.equals(apiKeyRecibida)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("API Key invalida.");
        }

        String token = jwtUtil.generarToken("canal-atm", "ROLE_ATM");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", "Bearer",
                "expiraEn", "1 hora"
        ));
    }
}