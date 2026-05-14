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

    @DeleteMapping("/cuenta")
    public ResponseEntity<String> eliminarCuenta(Authentication authentication) {
        AuthenticatedUser usuario = (AuthenticatedUser) authentication.getPrincipal();
        usuarioRepository.deleteById(usuario.getId());
        return ResponseEntity.ok("Cuenta eliminada correctamente.");
    }
}