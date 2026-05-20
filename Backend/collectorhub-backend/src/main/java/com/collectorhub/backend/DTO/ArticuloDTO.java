package com.collectorhub.backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class ArticuloDTO {

    //Categoria a la que pertenece el articulo
    @NotNull
    private Integer categoriaId;

    //Estado por defecto Coleccion, la otra alternativa es Wishlist
    private String estado = "COLECCION";

    // Campos dinamicos del articulo segun el esquema de su categoria
    private Map<String, Object> datos;

    private String imagen1;

    private String imagen2;
}