package com.bank.bffmobile.controller;

import com.bank.bffmobile.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de autenticacion para el canal Movil.
 */
@RestController
public class AuthController {

    @Value("${bff.api-key}")
    private String apiKeyValida;

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // POST https://localhost:8082/auth/token
    // header: X-API-KEY: mobile-secret-456
    @PostMapping("/auth/token")
    public ResponseEntity<?> obtenerToken(@RequestHeader("X-API-KEY") String apiKeyRecibida) {

        if (!apiKeyValida.equals(apiKeyRecibida)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("API Key invalida.");
        }

        String token = jwtUtil.generarToken("canal-mobile", "ROLE_MOBILE");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", "Bearer",
                "expiraEn", "1 hora"
        ));
    }
}