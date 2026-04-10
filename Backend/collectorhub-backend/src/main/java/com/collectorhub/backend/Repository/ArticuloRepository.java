package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {
    // Buscar los articulos de una categoria especifica de un usuario especifico
    List<Articulo> findByCategoriaIdAndUsuarioId(Integer categoriaId, Integer usuarioId);
}