package com.alasur.service;

import com.alasur.exception.ReglaNegocioException;
import com.alasur.model.Aerolinea;
import com.alasur.model.Equipaje;
import com.alasur.model.Pasajero;
import com.alasur.model.ReservaCanonica;
import com.alasur.model.Tarifa;
import com.alasur.model.Vuelo;
import com.alasur.repository.ReservaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Service
public class TransformacionService {
    private static final DateTimeFormatter FECHA_AERONORTE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FECHA_CARIBEAIR = DateTimeFormatter.ofPattern("MM/dd/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final BigDecimal LIBRAS_A_KILOGRAMOS = new BigDecimal("0.45359237");
    private static final Pattern REFERENCIA_SURJET = Pattern.compile("SJ-\\d{5}");
    private static final Pattern MALETA_CARIBEAIR = Pattern.compile("(?i)^(\\d+)\\s+pieza(s)?$");
    private static final Pattern TARIFA_CARIBEAIR = Pattern.compile("^(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d{1,2})?$");

    private final ReservaRepository repository;
    private final NormalizacionService normalizacion;

    public TransformacionService(ReservaRepository repository, NormalizacionService normalizacion) {
        this.repository = repository;
        this.normalizacion = normalizacion;
    }

    public ReservaCanonica transformarAeroNorte(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El XML de AeroNorte está vacío.");
        }
        try {
            validarXmlAeroNorte(xml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            String pnr = textoXml(document, "pnr");
            String[] nombre = textoXml(document, "pasajero").split("/", 2);
            if (nombre.length != 2 || nombre[0].isBlank() || nombre[1].isBlank()) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El pasajero AeroNorte debe usar el formato APELLIDO/NOMBRE.");
            }
            LocalDate fecha = LocalDate.parse(textoXml(document, "fecha"), FECHA_AERONORTE);
            BigDecimal importe = normalizacion.decimalPositivo(textoXml(document, "tarifa"), "tarifa");
            BigDecimal peso = normalizacion.decimalPositivo(textoXml(document, "equipaje"), "equipaje");
            String menor = textoXml(document, "menor");
            NormalizacionService.TipoPasajero tipo = menor.equals("S")
                    ? NormalizacionService.TipoPasajero.MENOR
                    : NormalizacionService.TipoPasajero.ADULTO;
            Pasajero pasajero = normalizacion.pasajero(nombre[1], nombre[0], null, tipo);

            ReservaCanonica reserva = new ReservaCanonica(
                    UUID.randomUUID(), Aerolinea.AERONORTE, pnr,
                    new Vuelo(fecha, null, null), pasajero,
                    new Tarifa(importe.setScale(2, RoundingMode.HALF_UP), "MXN"),
                    new Equipaje(peso.setScale(2, RoundingMode.HALF_UP), null, "KG", peso.stripTrailingZeros().toPlainString(), null));
            return repository.guardar(reserva);
        } catch (ReglaNegocioException ex) {
            throw ex;
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La fecha de AeroNorte debe tener formato DD/MM/AAAA y ser válida.");
        } catch (Exception ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "No fue posible procesar el XML de AeroNorte: " + ex.getMessage());
        }
    }

    public ReservaCanonica transformarSurJet(JsonNode json) {
        String booking = texto(json, "booking");
        if (!REFERENCIA_SURJET.matcher(booking).matches()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La reserva SurJet debe usar el formato SJ- seguido de cinco dígitos.");
        }
        String firstName = texto(json, "firstName");
        String lastName = texto(json, "lastName");
        String fechaTexto = texto(json, "flightDate");
        OffsetDateTime fechaHora;
        try {
            fechaHora = OffsetDateTime.parse(fechaTexto);
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "flightDate debe usar ISO 8601 e incluir zona horaria, por ejemplo 2026-04-03T08:30:00-06:00.");
        }
        JsonNode fare = obligatorio(json, "fare");
        BigDecimal amount = decimal(fare, "amount");
        String currency = texto(fare, "currency").toUpperCase(Locale.ROOT);
        if (!currency.matches("[A-Z]{3}")) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La moneda debe ser un código de tres letras.");
        }
        int age = entero(json, "age");
        BigDecimal pounds = decimal(json, "bagsLb");
        BigDecimal kilograms = pounds.multiply(LIBRAS_A_KILOGRAMOS).setScale(2, RoundingMode.HALF_UP);
        Pasajero pasajero = normalizacion.pasajero(firstName, lastName, age, normalizacion.tipoPorEdad(age));
        ReservaCanonica reserva = new ReservaCanonica(
                UUID.randomUUID(), Aerolinea.SURJET, booking,
                new Vuelo(fechaHora.toLocalDate(), fechaHora.toLocalTime(), fechaHora.getOffset().toString()),
                pasajero,
                new Tarifa(amount.setScale(2, RoundingMode.HALF_UP), currency),
                new Equipaje(kilograms, null, "LB", pounds.stripTrailingZeros().toPlainString(), null));
        return repository.guardar(reserva);
    }

    public ReservaCanonica transformarCaribeAir(JsonNode json) {
        String id = texto(json, "id");
        if (!id.matches("\\d+")) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El identificador CaribeAir debe ser un entero positivo.");
        }
        String nombreCompleto = texto(json, "nombre");
        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length < 2) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El nombre CaribeAir debe incluir al menos nombre y apellido.");
        }
        String primerNombre = String.join(" ", java.util.Arrays.copyOf(partes, partes.length - 1));
        String apellido = partes[partes.length - 1];
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(texto(json, "fecha"), FECHA_CARIBEAIR);
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La fecha CaribeAir debe tener formato MM/DD/AAAA y ser válida.");
        }
        String tarifaTexto = texto(json, "precio");
        if (!TARIFA_CARIBEAIR.matcher(tarifaTexto).matches()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El precio CaribeAir debe ser un importe numérico con separador de miles opcional.");
        }
        BigDecimal importe = normalizacion.decimalPositivo(tarifaTexto.replace(",", ""), "precio");
        String maletaTexto = texto(json, "maleta");
        Matcher matcher = MALETA_CARIBEAIR.matcher(maletaTexto);
        if (!matcher.matches()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "maleta debe indicar la cantidad de piezas, por ejemplo 1 pieza.");
        }
        int piezas = Integer.parseInt(matcher.group(1));
        if (piezas < 1) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La cantidad de piezas debe ser mayor que cero.");
        }
        String tipoTexto = texto(json, "tipoPasajero");
        NormalizacionService.TipoPasajero tipo = switch (tipoTexto.toUpperCase(Locale.ROOT)) {
            case "ADT" -> NormalizacionService.TipoPasajero.ADULTO;
            case "CHD" -> NormalizacionService.TipoPasajero.MENOR;
            case "INF" -> NormalizacionService.TipoPasajero.INFANTE;
            default -> throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "tipoPasajero debe ser ADT, CHD o INF.");
        };
        Pasajero pasajero = normalizacion.pasajero(primerNombre, apellido, null, tipo);
        ReservaCanonica reserva = new ReservaCanonica(
                UUID.randomUUID(), Aerolinea.CARIBEAIR, id,
                new Vuelo(fecha, null, null), pasajero,
                new Tarifa(importe.setScale(2, RoundingMode.HALF_UP), "MXN"),
                new Equipaje(null, piezas, "PIEZA", maletaTexto, new BigDecimal("23.00")));
        return repository.guardar(reserva);
    }

    private void validarXmlAeroNorte(String xml) throws Exception {
        var schemaResource = new ClassPathResource("schemas/origen/aeronorte-reserva.xsd");
        var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        var schema = schemaFactory.newSchema(schemaResource.getURL());
        var validator = schema.newValidator();
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        validator.validate(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
    }

    private String textoXml(Document document, String tag) {
        var nodes = document.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent().isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El elemento " + tag + " es obligatorio.");
        }
        return nodes.item(0).getTextContent().trim();
    }

    private String texto(JsonNode node, String campo) {
        JsonNode value = obligatorio(node, campo);
        if (!value.isValueNode() || value.isNull() || value.asText().isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " es obligatorio.");
        }
        return value.asText().trim();
    }

    private JsonNode obligatorio(JsonNode node, String campo) {
        if (node == null || !node.has(campo) || node.get(campo).isNull()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " es obligatorio.");
        }
        return node.get(campo);
    }

    private BigDecimal decimal(JsonNode node, String campo) {
        JsonNode value = obligatorio(node, campo);
        if (!value.isNumber() || value.decimalValue().signum() <= 0) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " debe ser numérico y mayor que cero.");
        }
        return value.decimalValue();
    }

    private int entero(JsonNode node, String campo) {
        JsonNode value = obligatorio(node, campo);
        if (!value.canConvertToInt() || !value.isIntegralNumber()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " debe ser un número entero.");
        }
        return value.intValue();
    }
}
