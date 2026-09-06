package com.bank.bffatm.dto;

import java.math.BigDecimal;

/**
 * Lo que el cajero envia al pedir un retiro: solo el monto deseado.
 */
public class RetiroRequestDTO {

    private BigDecimal monto;

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}