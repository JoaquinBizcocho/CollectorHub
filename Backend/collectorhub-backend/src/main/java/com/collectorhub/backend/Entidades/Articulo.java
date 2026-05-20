package com.collectorhub.backend.Entidades;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import java.util.Map;

@Entity
@Table(name = "articulos")
@Data
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "estado", nullable = false)
    private String estado = "COLECCION";

    // Los datos del articulo se guardan como JSON en la Base de datos permitiendo campos dinamicos
    // sin necesidad de columnas fijas
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> datos;

    // las imagenes se guardan como Base64 en LONGTEXT por su tamaño
    @Column(columnDefinition = "LONGTEXT")
    private String imagen1;

    @Column(columnDefinition = "LONGTEXT")
    private String imagen2;
}