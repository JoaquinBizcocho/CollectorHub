package com.collectorhub.backend.Entidades;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "categorias")
@Data
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "es_oficial", columnDefinition = "boolean default false")
    private Boolean esOficial = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Map<String, String>> esquema;
}