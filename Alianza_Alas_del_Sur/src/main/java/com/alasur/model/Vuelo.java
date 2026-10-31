package com.alasur.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Vuelo(
        LocalDate fechaVuelo,
        LocalTime horaLocal,
        String desfaseUtc
) {
}
