package com.alasur.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservaCanonica(
        UUID idReserva,
        Aerolinea aerolinea,
        String referenciaOrigen,
        Vuelo vuelo,
        Pasajero pasajero,
        Tarifa tarifa,
        Equipaje equipaje
) {
}
