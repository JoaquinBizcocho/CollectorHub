package com.collectorhub.backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class ArticuloDTO {
    private Integer id;

    @NotNull(message = "El categoriaId es obligatorio")
    private Integer categoriaId;

    private Integer usuarioId; // No validamos, lo ignoramos y usamos el del token

    @NotNull(message = "Los datos del articulo no pueden ser nulos")
    private Map<String, Object> datos;

    private String imagen1;
    private String imagen2;
}