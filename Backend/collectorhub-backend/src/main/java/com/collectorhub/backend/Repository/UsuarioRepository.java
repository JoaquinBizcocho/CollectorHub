package com.collectorhub.backend.Repository;

import com.collectorhub.backend.Entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Busca un usuario por su alias
    Optional<Usuario> findByAlias(String alias);

    // Se usa para validar duplicados en el registro
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    // busca cuentas no verificadas cuyo registro expiró
    @Query("SELECT u FROM Usuario u WHERE u.cuentaActiva = false AND u.fechaRegistro < ?1")
    List<Usuario> buscarCuentasExpiradas(LocalDateTime fechaLimite);
}