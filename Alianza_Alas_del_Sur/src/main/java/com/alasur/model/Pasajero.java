package com.alasur.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Adulto.class, name = "ADULTO"),
        @JsonSubTypes.Type(value = Menor.class, name = "MENOR"),
        @JsonSubTypes.Type(value = Infante.class, name = "INFANTE")
})
public abstract class Pasajero {
    private final String nombre;
    private final String apellido;
    private final Integer edad;

    protected Pasajero(String nombre, String apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public Integer getEdad() {
        return edad;
    }
}
