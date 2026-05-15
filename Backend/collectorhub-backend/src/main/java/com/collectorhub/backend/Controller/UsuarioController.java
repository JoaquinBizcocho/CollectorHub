package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "https://collector-hub-frontend.vercel.app")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Elimina la cuenta del usuario autenticado, junto con todos sus datos en cascada
    @DeleteMapping("/cuenta")
    public ResponseEntity<String> eliminarCuenta(Authentication authentication) {
        // Sacamos el id del token para asegurarnos de que cada usuario solo puede borrarse a sí mismo
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        usuarioRepository.deleteById(usuario.getId());
        return ResponseEntity.ok("Cuenta eliminada correctamente.");
    }
}