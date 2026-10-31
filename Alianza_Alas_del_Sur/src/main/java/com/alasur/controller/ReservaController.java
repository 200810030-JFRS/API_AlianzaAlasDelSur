package com.alasur.controller;

import com.alasur.exception.ReglaNegocioException;
import com.alasur.model.Aerolinea;
import com.alasur.model.ReservaCanonica;
import com.alasur.repository.ReservaRepository;
import com.alasur.service.TransformacionService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReservaController {
    private final ReservaRepository repository;
    private final TransformacionService transformacionService;

    public ReservaController(ReservaRepository repository, TransformacionService transformacionService) {
        this.repository = repository;
        this.transformacionService = transformacionService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "servicio", "Alianza Alas del Sur");
    }

    @GetMapping("/reservas")
    public Map<String, Object> listar() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("total", repository.listar().size());
        respuesta.put("reservas", repository.listar());
        return respuesta;
    }

    @GetMapping("/reservas/{id}")
    public ReservaCanonica buscar(@PathVariable UUID id) {
        return repository.buscar(id).orElseThrow(() ->
                new ReglaNegocioException(HttpStatus.NOT_FOUND, "No existe una reserva con el identificador indicado."));
    }

    @PostMapping(value = "/transformaciones/aeronorte", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservaCanonica> transformarAeroNorte(@RequestBody String xml) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transformacionService.transformarAeroNorte(xml));
    }

    @PostMapping(value = "/transformaciones/surjet", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservaCanonica> transformarSurJet(@RequestBody JsonNode json) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transformacionService.transformarSurJet(json));
    }

    @PostMapping(value = "/transformaciones/caribeair", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservaCanonica> transformarCaribeAir(@RequestBody JsonNode json) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transformacionService.transformarCaribeAir(json));
    }
}
