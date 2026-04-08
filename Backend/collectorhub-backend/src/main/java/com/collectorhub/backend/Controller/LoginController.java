package com.collectorhub.backend.Controller;

import com.collectorhub.backend.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador REST encargado de gestionar la autenticación de los usuarios.
 * Expone los endpoints de la API relacionados con el acceso (Login) y permite 
 * peticiones de orígenes cruzados (CORS) para comunicarse sin problemas con el Frontend (React).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    /**
     * Inyección de dependencias del repositorio de usuarios.
     * Permite realizar consultas a la base de datos MySQL (JPA) sin escribir SQL manual.
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Procesa las peticiones HTTP POST enviadas a la ruta "/api/login".
     * Extrae las credenciales del cuerpo de la petición (JSON) y las verifica
     * de forma dinámica contra los registros de la base de datos.
     */
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credenciales) {
        String alias = credenciales.get("alias");
        String pass = credenciales.get("password");

        // BUSQUEDA REAL EN LA BASE DE DATOS
        return usuarioRepository.findByAlias(alias)
                .map(user -> {
                    if (user.getPassword().equals(pass)) {
                        return "¡Acceso concedido! Bienvenido " + user.getAlias();
                    }
                    return "Contraseña incorrecta.";
                })
                .orElse("El usuario no existe en la base de datos.");
    }
}