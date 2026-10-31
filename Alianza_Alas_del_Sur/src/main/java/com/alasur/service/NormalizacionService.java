package com.alasur.service;

import com.alasur.exception.ReglaNegocioException;
import com.alasur.model.Adulto;
import com.alasur.model.Infante;
import com.alasur.model.Menor;
import com.alasur.model.Pasajero;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class NormalizacionService {
    public String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public BigDecimal decimalPositivo(String valor, String campo) {
        try {
            BigDecimal numero = new BigDecimal(texto(valor, campo));
            if (numero.signum() <= 0) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " debe ser mayor que cero.");
            }
            return numero;
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El campo " + campo + " debe ser numérico.");
        }
    }

    public String titulo(String nombre) {
        String limpio = texto(nombre, "nombre").toLowerCase(Locale.ROOT);
        StringBuilder resultado = new StringBuilder();
        boolean inicio = true;
        for (char c : limpio.toCharArray()) {
            if (Character.isWhitespace(c) || c == '-' || c == '\'') {
                inicio = true;
                resultado.append(c);
            } else if (inicio) {
                resultado.append(Character.toUpperCase(c));
                inicio = false;
            } else {
                resultado.append(c);
            }
        }
        return resultado.toString().trim();
    }

    public Pasajero pasajero(String nombre, String apellido, Integer edad, TipoPasajero tipo) {
        String nombreNormalizado = titulo(nombre);
        String apellidoNormalizado = titulo(apellido);
        return switch (tipo) {
            case ADULTO -> new Adulto(nombreNormalizado, apellidoNormalizado, edad);
            case MENOR -> new Menor(nombreNormalizado, apellidoNormalizado, edad);
            case INFANTE -> new Infante(nombreNormalizado, apellidoNormalizado, edad);
        };
    }

    public TipoPasajero tipoPorEdad(int edad) {
        if (edad < 0) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La edad no puede ser negativa.");
        }
        if (edad < 2) return TipoPasajero.INFANTE;
        if (edad < 12) return TipoPasajero.MENOR;
        return TipoPasajero.ADULTO;
    }

    public enum TipoPasajero {
        ADULTO,
        MENOR,
        INFANTE
    }
}
