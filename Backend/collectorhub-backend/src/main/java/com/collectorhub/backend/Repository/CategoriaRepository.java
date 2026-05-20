package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    // Devuelve todas las categorias de un usuario en concreto
    List<Categoria> findByUsuarioId(Integer usuarioId);

    // Devuelve las plantillas oficiales creadas por el admin
    List<Categoria> findByEsOficialTrue();
}