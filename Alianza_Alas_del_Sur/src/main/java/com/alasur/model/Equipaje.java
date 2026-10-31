package com.alasur.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Equipaje(
        BigDecimal pesoTotalKg,
        Integer numeroPiezas,
        String unidadOrigen,
        String valorOrigen,
        BigDecimal limiteKgPorPieza
) {
}
