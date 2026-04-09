package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.UsuarioRepository;
import com.collectorhub.backend.Entidades.Usuario; // Asegúrate de que esta ruta coincida con tu clase Usuario
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST encargado de gestionar la autenticación y registro de los usuarios.
 * Expone los endpoints de la API relacionados con el acceso (Login) y creación de cuentas (Register),
 * permitiendo peticiones de orígenes cruzados (CORS) para comunicarse con el Frontend (React).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Procesa las peticiones HTTP POST enviadas a la ruta "/api/login".
     * Extrae las credenciales y las verifica devolviendo un JSON (Map)
     * con el mensaje y el ID del usuario si el acceso es correcto.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String alias = credenciales.get("alias");
        String pass = credenciales.get("password");

        return usuarioRepository.findByAlias(alias)
                .map(user -> {
                    System.out.println("Usuario encontrado en BD: " + user.getAlias());
                    System.out.println("Hash guardado en BD: " + user.getPassword());
                    System.out.println("Longitud del Hash: " + user.getPassword().length() + " (debería ser 60)");

                    if (passwordEncoder.matches(pass, user.getPassword())) {
                        System.out.println("Resultado: ✅ COINCIDEN");
                        Map<String, Object> respuesta = new HashMap<>();
                        respuesta.put("mensaje", "✅ Acceso concedido");
                        respuesta.put("usuarioId", user.getId());
                        return ResponseEntity.ok(respuesta);
                    } else {
                        System.out.println("Resultado: ❌ NO COINCIDEN");
                        Map<String, String> error = new HashMap<>();
                        error.put("mensaje", "❌ Contraseña incorrecta.");
                        return ResponseEntity.badRequest().body(error);
                    }
                })
                .orElseGet(() -> {
                    System.out.println("Resultado: ❌ USUARIO NO ENCONTRADO");
                    Map<String, String> error = new HashMap<>();
                    error.put("mensaje", "❌ El usuario no existe en la base de datos.");
                    return ResponseEntity.badRequest().body(error);
                });
    }

    /**
     * Procesa las peticiones HTTP POST enviadas a la ruta "/api/register".
     * Crea un nuevo usuario validando que el alias y el correo no existan previamente.
     */
    @PostMapping("/register")
    public String registrarUsuario(@RequestBody Map<String, String> datosUsuario) {
        String alias = datosUsuario.get("alias");
        String correo = datosUsuario.get("correo_electronico");
        String passPlana = datosUsuario.get("password");

        // 1. Comprobar que no faltan datos
        if (alias == null || correo == null || passPlana == null) {
            return "❌ Error: Faltan datos obligatorios.";
        }

        // 2. Comprobar que el alias no existe
        if (usuarioRepository.existsByAlias(alias)) {
            return "❌ Error: El alias ya está en uso. Elige otro.";
        }

        // 3. Comprobar que el correo no existe
        if (usuarioRepository.comprobarSiExisteCorreo(correo)) {
            return "❌ Error: Ya existe una cuenta con este correo electrónico.";
        }

        // 4. Encriptar la contraseña
        String passEncriptada = passwordEncoder.encode(passPlana);

        // 5. Crear el nuevo usuario y guardarlo
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAlias(alias);
        nuevoUsuario.setCorreo_electronico(correo);
        nuevoUsuario.setPassword(passEncriptada);
        nuevoUsuario.setRol("user");

        usuarioRepository.save(nuevoUsuario);

        return "✅ ¡Usuario registrado con éxito! Ya puedes iniciar sesión.";
    }
}