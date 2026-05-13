package com.collectorhub.backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class ArticuloDTO {

    @NotNull
    private Integer categoriaId;

    private String estado = "COLECCION";

    private Map<String, Object> datos;

    private String imagen1;

    private String imagen2;
}