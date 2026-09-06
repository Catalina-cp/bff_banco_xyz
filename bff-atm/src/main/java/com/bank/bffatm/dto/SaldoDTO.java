package com.bank.bffatm.dto;

import java.math.BigDecimal;

/**
 * Respuesta minima para el canal Cajero Automatico: solo el saldo,
 * sin nombre ni otros datos personales, por seguridad en un dispositivo
 * publico.
 */
public class SaldoDTO {

    private Long cuentaId;
    private BigDecimal saldoDisponible;

    public SaldoDTO(Long cuentaId, BigDecimal saldoDisponible) {
        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }
}