package com.bank.bffmobile.controller;

import com.bank.bffmobile.dto.CuentaResumenDTO;
import com.bank.bffmobile.repository.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BFF Movil: expone solo los datos ESENCIALES de cada cuenta (id, nombre,
 * saldo final), pensado para reducir el consumo de datos moviles y
 * acelerar la carga en la app.
 */
@RestController
@RequestMapping("/api/mobile/cuentas")
public class CuentaController {

    @Autowired
    private InterestRepository interestRepository;

    // GET http://localhost:8082/api/mobile/cuentas
    @GetMapping
    public List<CuentaResumenDTO> listarTodas() {
        return interestRepository.findAll()
                .stream()
                .map(cuenta -> new CuentaResumenDTO(
                        cuenta.getCuentaId(),
                        cuenta.getNombre(),
                        cuenta.getSaldoFinal()
                ))
                .toList();
    }

    // GET http://localhost:8082/api/mobile/cuentas/101
    @GetMapping("/{cuentaId}")
    public CuentaResumenDTO buscarPorId(@PathVariable Long cuentaId) {
        var cuenta = interestRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));

        return new CuentaResumenDTO(
                cuenta.getCuentaId(),
                cuenta.getNombre(),
                cuenta.getSaldoFinal()
        );
    }
}