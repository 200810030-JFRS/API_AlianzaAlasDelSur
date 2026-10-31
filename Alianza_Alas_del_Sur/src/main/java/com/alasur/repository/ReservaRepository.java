package com.alasur.repository;

import com.alasur.exception.ReglaNegocioException;
import com.alasur.model.Aerolinea;
import com.alasur.model.ReservaCanonica;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class ReservaRepository {
    private final ConcurrentHashMap<UUID, ReservaCanonica> reservas = new ConcurrentHashMap<>();

    public synchronized ReservaCanonica guardar(ReservaCanonica reserva) {
        boolean referenciaDuplicada = reservas.values().stream().anyMatch(actual ->
                actual.aerolinea() == reserva.aerolinea()
                        && actual.referenciaOrigen().equalsIgnoreCase(reserva.referenciaOrigen()));
        if (referenciaDuplicada) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "La referencia de origen ya fue procesada para esta aerolínea.");
        }
        reservas.put(reserva.idReserva(), reserva);
        return reserva;
    }

    public List<ReservaCanonica> listar() {
        return reservas.values().stream()
                .sorted(Comparator.comparing(ReservaCanonica::idReserva))
                .toList();
    }

    public Optional<ReservaCanonica> buscar(UUID id) {
        return Optional.ofNullable(reservas.get(id));
    }

    public long contarPorAerolinea(Aerolinea aerolinea) {
        return reservas.values().stream().filter(r -> r.aerolinea() == aerolinea).count();
    }
}
