package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByUsuarioId(Integer usuarioId);
}