package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Spring entiende que quieres buscar por la columna 'alias'
    Optional<Usuario> findByAlias(String alias);
}