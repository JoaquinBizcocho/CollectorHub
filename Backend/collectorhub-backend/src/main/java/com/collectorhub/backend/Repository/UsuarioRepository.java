package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByAlias(String alias);

    boolean existsByAlias(String alias);

    // Para validar correo duplicado
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.correoElectronico = ?1")
    boolean comprobarSiExisteCorreo(String correo);

    // busca cuentas no verificadas cuyo registro expiró
    List<Usuario> buscaCuentaInactiva(LocalDateTime fechaLimite);
}