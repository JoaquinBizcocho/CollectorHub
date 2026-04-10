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

    // Aqui guardaremos las respuestas dinamicas  {"PSA": "10", "Año": 2019}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> datos;

    // LONGTEXT es necesario porque las imagenes en Base64 ocupan muchos caracteres
    @Column(columnDefinition = "LONGTEXT")
    private String imagen1;

    @Column(columnDefinition = "LONGTEXT")
    private String imagen2;
}