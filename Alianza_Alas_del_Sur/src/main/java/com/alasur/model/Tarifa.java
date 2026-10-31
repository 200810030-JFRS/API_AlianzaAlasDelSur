package com.alasur.model;

import java.math.BigDecimal;

public record Tarifa(
        BigDecimal importe,
        String moneda
) {
}
