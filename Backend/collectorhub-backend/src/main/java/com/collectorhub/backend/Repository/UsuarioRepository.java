package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByAlias(String alias);

    boolean existsByAlias(String alias);

    // Le decimos exactamente la consulta que debe hacer para evitar la trampa de la barra baja
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.correoElectronico = ?1")
    boolean comprobarSiExisteCorreo(String correo);
}