package com.bank.bffweb.controller;

import com.bank.bffweb.model.Interest;
import com.bank.bffweb.repository.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BFF Web: expone los datos COMPLETOS de cada cuenta, pensado para un
 * dashboard de navegador donde se muestra toda la informacion disponible.
 */
@RestController
@RequestMapping("/api/web/cuentas")
public class CuentaController {

    @Autowired
    private InterestRepository interestRepository;

    // GET http://localhost:8081/api/web/cuentas
    @GetMapping
    public List<Interest> listarTodas() {
        return interestRepository.findAll();
    }

    // GET http://localhost:8081/api/web/cuentas/101
    @GetMapping("/{cuentaId}")
    public Interest buscarPorId(@PathVariable Long cuentaId) {
        return interestRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
    }
}