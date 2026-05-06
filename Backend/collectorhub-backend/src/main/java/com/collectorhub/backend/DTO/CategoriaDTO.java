package com.collectorhub.backend.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CategoriaDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer usuarioId;
    private Boolean esOficial;
    private List<Map<String, String>> esquema;
}