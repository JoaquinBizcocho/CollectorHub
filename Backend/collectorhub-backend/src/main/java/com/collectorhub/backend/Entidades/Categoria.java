package com.collectorhub.backend.Entidades;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "categorias")
@Data // Lombok nos crea los getters y setters automáticamente
public class Categoria {

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Magia pura: Le decimos a Spring que guarde esta lista como un JSON real en MySQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Map<String, String>> esquema;

}