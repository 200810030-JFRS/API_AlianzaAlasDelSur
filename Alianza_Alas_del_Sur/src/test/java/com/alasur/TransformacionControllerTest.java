package com.alasur;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TransformacionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthResponde() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void transformaAeroNorte() throws Exception {
        String xml = "<reserva><pnr>AA1234</pnr><pasajero>GARCIA/ANA</pasajero><fecha>03/04/2026</fecha><tarifa>4500</tarifa><equipaje>23</equipaje><menor>N</menor></reserva>";
        mockMvc.perform(post("/api/v1/transformaciones/aeronorte")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aerolinea").value("AERONORTE"))
                .andExpect(jsonPath("$.tarifa.moneda").value("MXN"))
                .andExpect(jsonPath("$.pasajero.tipo").value("ADULTO"));
    }

    @Test
    void transformaSurJetYConvierteLibras() throws Exception {
        String json = """
                {"booking":"SJ-12345","firstName":"Ana","lastName":"García","flightDate":"2026-04-03T08:30:00-06:00","fare":{"amount":260,"currency":"USD"},"bagsLb":50,"age":34}
                """;
        mockMvc.perform(post("/api/v1/transformaciones/surjet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipaje.pesoTotalKg").value(22.68))
                .andExpect(jsonPath("$.tarifa.moneda").value("USD"))
                .andExpect(jsonPath("$.vuelo.desfaseUtc").value("-06:00"));
    }

    @Test
    void transformaCaribeAir() throws Exception {
        String json = """
                {"id":99812,"nombre":"Ana Garcia","fecha":"04/03/2026","precio":"4,500.00","maleta":"1 pieza","tipoPasajero":"ADT"}
                """;
        mockMvc.perform(post("/api/v1/transformaciones/caribeair")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipaje.numeroPiezas").value(1))
                .andExpect(jsonPath("$.tarifa.importe").value(4500.00))
                .andExpect(jsonPath("$.vuelo.fechaVuelo").value("2026-04-03"));
    }
}
