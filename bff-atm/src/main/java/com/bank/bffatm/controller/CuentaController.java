package com.bank.bffatm.controller;

import com.bank.bffatm.dto.RetiroRequestDTO;
import com.bank.bffatm.dto.SaldoDTO;
import com.bank.bffatm.model.Interest;
import com.bank.bffatm.repository.InterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * BFF Cajero Automatico: interfaz segura y minima para operaciones
 * criticas (consulta de saldo y retiro), con validaciones estrictas
 * ya que involucra movimiento real de dinero.
 */
@RestController
@RequestMapping("/api/atm/cuentas")
public class CuentaController {

    @Autowired
    private InterestRepository interestRepository;

    // GET http://localhost:8083/api/atm/cuentas/101/saldo
    @GetMapping("/{cuentaId}/saldo")
    public SaldoDTO consultarSaldo(@PathVariable Long cuentaId) {
        Interest cuenta = interestRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));

        return new SaldoDTO(cuenta.getCuentaId(), cuenta.getSaldoFinal());
    }

    // POST http://localhost:8083/api/atm/cuentas/101/retiro
    // body: { "monto": 500 }
    @PostMapping("/{cuentaId}/retiro")
    public ResponseEntity<?> retirar(@PathVariable Long cuentaId,
                                      @RequestBody RetiroRequestDTO request) {

        Interest cuenta = interestRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));

        BigDecimal monto = request.getMonto();

        // Validacion 1: el monto debe existir y ser positivo
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body("El monto a retirar debe ser mayor a 0.");
        }

        // Validacion 2: no se puede retirar mas de lo disponible
        if (monto.compareTo(cuenta.getSaldoFinal()) > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Fondos insuficientes. Saldo disponible: " + cuenta.getSaldoFinal());
        }

        // Retiro valido: se descuenta el monto y se guarda
        cuenta.setSaldoFinal(cuenta.getSaldoFinal().subtract(monto));
        interestRepository.save(cuenta);

        return ResponseEntity.ok(new SaldoDTO(cuenta.getCuentaId(), cuenta.getSaldoFinal()));
    }
}