package com.collectorhub.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CategoriaDTO {
    private Integer id;

    @NotBlank(message = "El nombre de la categoria no puede estar vacio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
    private String descripcion;

    private Integer usuarioId;
    // Es true si la crea un admin como plantilla oficial
    private Boolean esOficial;

    // Define los campos que tendran los articulos de esta categoria
    @NotEmpty(message = "La categoria debe tener al menos un campo en el esquema")
    private List<Map<String, String>> esquema;
}