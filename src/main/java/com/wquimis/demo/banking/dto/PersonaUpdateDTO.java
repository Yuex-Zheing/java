package com.wquimis.demo.banking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PersonaUpdateDTO {
    @NotBlank(message = "El nombre es requerido")
    private String nombres;

    @NotBlank(message = "El género es requerido")
    @Size(min = 1, max = 1, message = "El género debe ser un solo carácter (M o F)")
    private String genero;

    @Min(value = 0, message = "La edad debe ser un número positivo")
    private int edad;

    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    @NotBlank(message = "El teléfono es requerido")
    private String telefono;

    private Boolean estado;

    // Getters y Setters
    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
