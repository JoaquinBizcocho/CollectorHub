package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Entidades.Usuario;
import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.security.JwtUtil; // ¡Importante!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil; // Inyectamos nuestra herramienta

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciales) {
        String alias = credenciales.get("alias");
        String pass = credenciales.get("password");

        Optional<Usuario> optionalUser = usuarioRepository.findByAlias(alias);

        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (passwordEncoder.matches(pass, user.getPassword())) {
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Acceso concedido");
                respuesta.put("usuarioId", user.getId());
                respuesta.put("rol", user.getRol());

                // --- GENERAMOS EL TOKEN SEGURO ---
                String token = jwtUtil.generarToken(user.getId(), user.getAlias(), user.getRol());
                respuesta.put("token", token);

                return ResponseEntity.ok(respuesta);
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put("mensaje", "Credenciales incorrectas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}