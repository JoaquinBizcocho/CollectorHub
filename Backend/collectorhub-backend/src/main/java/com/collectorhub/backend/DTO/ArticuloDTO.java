package com.collectorhub.backend.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class ArticuloDTO {
    private Integer id;
    private Integer categoriaId;
    private Integer usuarioId;
    private Map<String, Object> datos;
    private String imagen1;
    private String imagen2;
}