package com.bank.bffmobile.dto;

import java.math.BigDecimal;

/**
 * Respuesta liviana para el canal Movil: solo los datos esenciales,
 * para reducir el consumo de ancho de banda frente al BFF Web (que
 * devuelve la cuenta completa con todos sus campos).
 */
public class CuentaResumenDTO {

    private Long cuentaId;
    private String nombre;
    private BigDecimal saldoFinal;

    public CuentaResumenDTO(Long cuentaId, String nombre, BigDecimal saldoFinal) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldoFinal = saldoFinal;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }
}